package com.example.device;

import com.example.device.service.DeviceFileService;
import com.example.device.service.DeviceService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.scheduler.overdue-cron=0 0 0 1 1 *",
        "app.scheduler.daily-overdue-report-cron=0 0 0 1 1 *"
})
@ActiveProfiles("test")
class DeviceScaleBenchmarkIT {

    private static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();
    private static final int PAGE_SIZE = 100;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceFileService deviceFileService;

    @Autowired
    private Environment environment;

    private final List<Measurement> measurements = new ArrayList<>();
    private final List<QueryPlan> queryPlans = new ArrayList<>();

    private Path datasetDirectory;
    private Path outputPath;
    private int repetitions;
    private int importRepetitions;
    private int warmups;
    private List<Integer> datasets;

    @BeforeAll
    static void configureProcess() {
        System.setProperty("java.awt.headless", "true");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("benchmark@device.local", "N/A", "ROLE_ADMIN")
        );
    }

    @AfterAll
    static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void benchmarkIsolatedDeviceDataScale() throws Exception {
        assertEquals("true", System.getProperty("device.benchmark.enabled"),
                "Run through scripts/benchmark-device-scale.sh with --confirm-isolated");

        datasetDirectory = requiredPath("device.benchmark.dataset-dir");
        outputPath = requiredPath("device.benchmark.output");
        repetitions = positiveInt("device.benchmark.repetitions", 3);
        importRepetitions = positiveInt("device.benchmark.import-repetitions", 1);
        warmups = positiveInt("device.benchmark.warmups", 1);
        datasets = parseDatasets(System.getProperty("device.benchmark.datasets", "1000,10000,100000"));

        assertIsolatedDatasource();
        warmUpImportInfrastructure();

        for (int rows : datasets) {
            Path csv = datasetDirectory.resolve("devices-" + rows + ".csv");
            assertTrue(Files.isRegularFile(csv), "Missing generated dataset: " + csv);

            benchmarkImport(rows, csv);
            assertEquals(rows, countDevices(), "Import row count mismatch for dataset " + rows);

            benchmarkRead("page-first", rows,
                    () -> deviceService.searchDevices(null, null, null, 0, PAGE_SIZE).getNumberOfElements());

            int deepPage = Math.max(0, (rows - 1) / PAGE_SIZE);
            benchmarkRead("page-deep", rows,
                    () -> deviceService.searchDevices(null, null, null, deepPage, PAGE_SIZE).getNumberOfElements());

            String tailKeyword = String.format(Locale.ROOT, "Scale Device %07d", rows);
            benchmarkRead("keyword-tail", rows,
                    () -> deviceService.searchDevices(tailKeyword, null, null, 0, PAGE_SIZE).getTotalElements());

            benchmarkExport("export-csv", rows, deviceFileService::exportCsv);
            benchmarkExport("export-xlsx", rows, deviceFileService::exportExcel);
            capturePlans(rows, deepPage, tailKeyword);
            writeReport();
        }

        writeReport();
        assertTrue(Files.size(outputPath) > 0, "Benchmark report was not written");
    }

    private void benchmarkImport(int rows, Path csv) throws Exception {
        for (int run = 1; run <= importRepetitions; run++) {
            deleteDeviceData();
            PathMultipartFile file = new PathMultipartFile(csv);
            Measurement measurement = measure(rows, "import-csv", run,
                    () -> deviceFileService.importCsv(file));
            assertEquals(rows, measurement.units());
            assertEquals(rows, countDevices());
            measurements.add(measurement);
        }
    }

    private void warmUpImportInfrastructure() throws Exception {
        Path smallest = datasetDirectory.resolve("devices-" + datasets.get(0) + ".csv");
        for (int i = 0; i < warmups; i++) {
            deleteDeviceData();
            deviceFileService.importCsv(new PathMultipartFile(smallest));
        }
        deleteDeviceData();
    }

    private void benchmarkRead(String operation, int rows, LongSupplier action) throws Exception {
        warmUp(action);
        for (int run = 1; run <= repetitions; run++) {
            measurements.add(measure(rows, operation, run, action));
        }
    }

    private void benchmarkExport(String operation, int rows, ExportAction action) throws Exception {
        for (int i = 0; i < warmups; i++) {
            action.writeTo(OutputStream.nullOutputStream());
        }

        for (int run = 1; run <= repetitions; run++) {
            CountingOutputStream output = new CountingOutputStream();
            measurements.add(measure(rows, operation, run, () -> {
                action.writeTo(output);
                return output.count();
            }));
        }
    }

    private void warmUp(LongSupplier action) {
        for (int i = 0; i < warmups; i++) {
            action.getAsLong();
        }
    }

    private Measurement measure(int rows, String operation, int run, LongSupplier action) throws Exception {
        System.gc();
        Thread.sleep(100);

        long baselineHeap = usedHeapBytes();
        long baselineRss = residentSetBytes();
        AtomicLong peakHeap = new AtomicLong(baselineHeap);
        AtomicLong peakRss = new AtomicLong(baselineRss);
        AtomicBoolean sampling = new AtomicBoolean(true);
        Thread sampler = new Thread(() -> sampleMemory(sampling, peakHeap, peakRss), "benchmark-memory-sampler");
        sampler.setDaemon(true);

        long started = System.nanoTime();
        sampler.start();
        long units;
        try {
            units = action.getAsLong();
        } finally {
            sampling.set(false);
            sampler.join();
        }
        long elapsedNanos = System.nanoTime() - started;

        peakHeap.accumulateAndGet(usedHeapBytes(), Math::max);
        peakRss.accumulateAndGet(residentSetBytes(), Math::max);
        return new Measurement(
                rows,
                operation,
                run,
                elapsedNanos / 1_000_000.0,
                bytesToMiB(baselineHeap),
                bytesToMiB(peakHeap.get()),
                bytesToMiB(Math.max(0, peakHeap.get() - baselineHeap)),
                bytesToMiB(baselineRss),
                bytesToMiB(peakRss.get()),
                bytesToMiB(Math.max(0, peakRss.get() - baselineRss)),
                units
        );
    }

    private void sampleMemory(AtomicBoolean sampling, AtomicLong peakHeap, AtomicLong peakRss) {
        while (sampling.get()) {
            peakHeap.accumulateAndGet(usedHeapBytes(), Math::max);
            peakRss.accumulateAndGet(residentSetBytes(), Math::max);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void capturePlans(int rows, int deepPage, String tailKeyword) {
        int offset = deepPage * PAGE_SIZE;
        String escapedKeyword = tailKeyword.toLowerCase(Locale.ROOT).replace("'", "''");

        addPlan(rows, "page-deep-data", """
                SELECT id, category, description, model, name, serial_number, state,
                       updated_by, updated_time, version
                FROM devices
                ORDER BY name, id
                LIMIT %d OFFSET %d
                """.formatted(PAGE_SIZE, offset));
        addPlan(rows, "page-count", "SELECT COUNT(id) FROM devices");
        addPlan(rows, "keyword-tail-data", """
                SELECT id, category, description, model, name, serial_number, state,
                       updated_by, updated_time, version
                FROM devices
                WHERE LOWER(name) LIKE '%%%1$s%%'
                   OR LOWER(model) LIKE '%%%1$s%%'
                   OR LOWER(serial_number) LIKE '%%%1$s%%'
                ORDER BY name, id
                LIMIT %2$d OFFSET 0
                """.formatted(escapedKeyword, PAGE_SIZE));
        addPlan(rows, "keyword-tail-count", """
                SELECT COUNT(id)
                FROM devices
                WHERE LOWER(name) LIKE '%%%1$s%%'
                   OR LOWER(model) LIKE '%%%1$s%%'
                   OR LOWER(serial_number) LIKE '%%%1$s%%'
                """.formatted(escapedKeyword));
    }

    private void addPlan(int rows, String name, String sql) {
        String plan = jdbcTemplate.query("EXPLAIN ANALYZE " + sql, resultSet -> {
            StringBuilder value = new StringBuilder();
            while (resultSet.next()) {
                if (!value.isEmpty()) {
                    value.append(System.lineSeparator());
                }
                value.append(resultSet.getString(1));
            }
            return value.toString();
        });
        queryPlans.add(new QueryPlan(rows, name, sql.strip(), plan));
    }

    private void assertIsolatedDatasource() {
        String configuredUrl = environment.getProperty("spring.datasource.url", "")
                .toLowerCase(Locale.ROOT);
        assertTrue(configuredUrl.startsWith("jdbc:tc:mysql:"),
                "Benchmark datasource is not configured as jdbc:tc:mysql");
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        assertEquals("device_test", database);
    }

    private void deleteDeviceData() {
        jdbcTemplate.update("DELETE FROM assignment_extensions");
        jdbcTemplate.update("DELETE FROM device_repairs");
        jdbcTemplate.update("DELETE FROM device_assignments");
        jdbcTemplate.update("DELETE FROM devices");
    }

    private long countDevices() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM devices", Long.class);
        return count == null ? 0 : count;
    }

    private void writeReport() throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder report = new StringBuilder();
        report.append("# Device data-scale baseline\n\n")
                .append("Generated: ").append(ZonedDateTime.now()).append("\n\n")
                .append("## Environment\n\n")
                .append("| Field | Value |\n|---|---|\n")
                .append("| Commit | `").append(System.getProperty("device.benchmark.commit", "unknown")).append("` |\n")
                .append("| OS | ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append(' ')
                .append(System.getProperty("os.arch")).append(" |\n")
                .append("| CPU | ").append(cpuModel()).append(" |\n")
                .append("| Available processors | ").append(Runtime.getRuntime().availableProcessors()).append(" |\n")
                .append("| Physical RAM | ").append(formatMiB(totalPhysicalMemoryBytes())).append(" MiB |\n")
                .append("| JVM | ").append(System.getProperty("java.vm.name")).append(' ')
                .append(System.getProperty("java.version")).append(" |\n")
                .append("| JVM max heap | ").append(formatMiB(MEMORY.getHeapMemoryUsage().getMax())).append(" MiB |\n")
                .append("| MySQL | ").append(jdbcTemplate.queryForObject("SELECT VERSION()", String.class)).append(" |\n")
                .append("| Datasource | Testcontainers `device_test` (disposable) |\n")
                .append("| Datasets | ").append(datasets).append(" rows |\n")
                .append("| Warm-ups | ").append(warmups).append(" per read/export; import infrastructure warmed with smallest dataset |\n")
                .append("| Recorded repetitions | ").append(repetitions)
                .append(" per read/export; ").append(importRepetitions)
                .append(" per import and dataset |\n")
                .append("| Memory sampling | JVM heap + Linux `/proc/self/status` VmRSS every 10 ms |\n\n")
                .append("`units` is imported rows, page result count, or exported bytes depending on operation. ")
                .append("Memory deltas are relative to the post-GC baseline immediately before each recorded run.\n\n")
                .append("## Raw measurements\n\n")
                .append("| rows | operation | run | latency_ms | heap_base_mib | heap_peak_mib | heap_delta_mib | rss_base_mib | rss_peak_mib | rss_delta_mib | units |\n")
                .append("|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");

        for (Measurement measurement : measurements) {
            report.append(measurement.markdownRow());
        }

        report.append("\n## Summary (median when repeated)\n\n")
                .append("| rows | operation | median_latency_ms | max_heap_delta_mib | max_rss_delta_mib |\n")
                .append("|---:|---|---:|---:|---:|\n");

        datasets.forEach(rows -> measurements.stream()
                .filter(value -> value.rows() == rows)
                .map(Measurement::operation)
                .distinct()
                .forEach(operation -> appendSummary(report, rows, operation)));

        report.append("\n## EXPLAIN ANALYZE baseline\n\n")
                .append("Plans are captured after measurements on the same dataset, before any PRD-209 index change.\n\n");
        for (QueryPlan queryPlan : queryPlans) {
            report.append("### ").append(queryPlan.rows()).append(" rows — ")
                    .append(queryPlan.name()).append("\n\n")
                    .append("```sql\n").append(queryPlan.sql()).append("\n```\n\n")
                    .append("```text\n").append(queryPlan.plan()).append("\n```\n\n");
        }

        Files.writeString(outputPath, report.toString(), StandardCharsets.UTF_8);
    }

    private void appendSummary(StringBuilder report, int rows, String operation) {
        List<Measurement> values = measurements.stream()
                .filter(value -> value.rows() == rows && value.operation().equals(operation))
                .sorted(Comparator.comparingDouble(Measurement::latencyMs))
                .toList();
        double median = values.get(values.size() / 2).latencyMs();
        double heapDelta = values.stream().mapToDouble(Measurement::heapDeltaMiB).max().orElse(0);
        double rssDelta = values.stream().mapToDouble(Measurement::rssDeltaMiB).max().orElse(0);
        report.append(String.format(Locale.ROOT, "| %d | %s | %.3f | %.3f | %.3f |%n",
                rows, operation, median, heapDelta, rssDelta));
    }

    private Path requiredPath(String property) {
        String value = System.getProperty(property);
        assertTrue(value != null && !value.isBlank(), "Missing system property: " + property);
        return Path.of(value).toAbsolutePath().normalize();
    }

    private int positiveInt(String property, int fallback) {
        int value = Integer.parseInt(System.getProperty(property, Integer.toString(fallback)));
        assertTrue(value > 0, property + " must be positive");
        return value;
    }

    private List<Integer> parseDatasets(String value) {
        List<Integer> parsed = Arrays.stream(value.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .toList();
        assertTrue(!parsed.isEmpty() && parsed.stream().allMatch(rows -> rows > 0 && rows <= 100_000),
                "Datasets must be between 1 and 100000 rows");
        return parsed;
    }

    private long usedHeapBytes() {
        return MEMORY.getHeapMemoryUsage().getUsed();
    }

    private long residentSetBytes() {
        Path status = Path.of("/proc/self/status");
        if (!Files.isReadable(status)) {
            return 0;
        }
        try {
            return Files.readAllLines(status).stream()
                    .filter(line -> line.startsWith("VmRSS:"))
                    .map(line -> line.replaceAll("[^0-9]", ""))
                    .filter(value -> !value.isBlank())
                    .mapToLong(Long::parseLong)
                    .map(value -> value * 1024)
                    .findFirst()
                    .orElse(0);
        } catch (IOException e) {
            return 0;
        }
    }

    private long totalPhysicalMemoryBytes() {
        if (ManagementFactory.getOperatingSystemMXBean()
                instanceof com.sun.management.OperatingSystemMXBean operatingSystem) {
            return operatingSystem.getTotalMemorySize();
        }
        return 0;
    }

    private String cpuModel() {
        Path cpuInfo = Path.of("/proc/cpuinfo");
        if (!Files.isReadable(cpuInfo)) {
            return "unknown";
        }
        try {
            return Files.readAllLines(cpuInfo).stream()
                    .filter(line -> line.toLowerCase(Locale.ROOT).startsWith("model name"))
                    .map(line -> line.substring(line.indexOf(':') + 1).trim())
                    .findFirst()
                    .orElse("unknown")
                    .replace("|", "\\|");
        } catch (IOException e) {
            return "unknown";
        }
    }

    private double bytesToMiB(long bytes) {
        return bytes / 1024.0 / 1024.0;
    }

    private String formatMiB(long bytes) {
        return String.format(Locale.ROOT, "%.1f", bytesToMiB(bytes));
    }

    private record Measurement(
            int rows,
            String operation,
            int run,
            double latencyMs,
            double heapBaseMiB,
            double heapPeakMiB,
            double heapDeltaMiB,
            double rssBaseMiB,
            double rssPeakMiB,
            double rssDeltaMiB,
            long units
    ) {
        String markdownRow() {
            return String.format(Locale.ROOT,
                    "| %d | %s | %d | %.3f | %.3f | %.3f | %.3f | %.3f | %.3f | %.3f | %d |%n",
                    rows, operation, run, latencyMs, heapBaseMiB, heapPeakMiB, heapDeltaMiB,
                    rssBaseMiB, rssPeakMiB, rssDeltaMiB, units);
        }
    }

    private record QueryPlan(int rows, String name, String sql, String plan) {
    }

    @FunctionalInterface
    private interface ExportAction {
        void writeTo(OutputStream outputStream);
    }

    private static final class CountingOutputStream extends OutputStream {
        private long count;

        @Override
        public void write(int value) {
            count++;
        }

        @Override
        public void write(byte[] values, int offset, int length) {
            count += length;
        }

        long count() {
            return count;
        }
    }

    private record PathMultipartFile(Path path) implements MultipartFile {
        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return path.getFileName().toString();
        }

        @Override
        public String getContentType() {
            return "text/csv";
        }

        @Override
        public boolean isEmpty() {
            return getSize() == 0;
        }

        @Override
        public long getSize() {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read benchmark dataset size", e);
            }
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(path);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public void transferTo(java.io.File destination) throws IOException {
            Files.copy(path, destination.toPath());
        }
    }
}
