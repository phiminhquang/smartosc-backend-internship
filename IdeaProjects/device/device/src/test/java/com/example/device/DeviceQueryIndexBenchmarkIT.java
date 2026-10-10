package com.example.device;

import com.example.device.service.DeviceService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.scheduler.overdue-cron=0 0 0 1 1 *",
        "app.scheduler.daily-overdue-report-cron=0 0 0 1 1 *"
})
@ActiveProfiles("test")
class DeviceQueryIndexBenchmarkIT {

    private static final String INDEX_NAME = "idx_devices_name";
    private static final String INDEX_COLUMNS = "name";
    private static final int PAGE_SIZE = 100;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private Environment environment;

    private final List<Measurement> measurements = new ArrayList<>();
    private final List<QueryPlan> queryPlans = new ArrayList<>();
    private final List<DdlMeasurement> ddlMeasurements = new ArrayList<>();

    private Path datasetPath;
    private Path outputPath;
    private int rows;
    private int repetitions;
    private int warmups;
    private int cycles;

    @BeforeAll
    static void configureProcess() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("index-benchmark@device.local", "N/A", "ROLE_ADMIN")
        );
    }

    @AfterAll
    static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void compareDeviceQueriesBeforeAndAfterCandidateIndex() throws Exception {
        assertEquals("true", System.getProperty("device.index-benchmark.enabled"),
                "Run through scripts/benchmark-device-query-index.sh with --confirm-isolated");

        datasetPath = requiredPath("device.index-benchmark.dataset");
        outputPath = requiredPath("device.index-benchmark.output");
        rows = positiveInt("device.index-benchmark.rows", 100_000);
        repetitions = positiveInt("device.index-benchmark.repetitions", 3);
        warmups = positiveInt("device.index-benchmark.warmups", 2);
        cycles = positiveInt("device.index-benchmark.cycles", 2);

        assertTrue(rows <= 100_000, "Benchmark rows must not exceed the generator safety limit");
        assertTrue(Files.isRegularFile(datasetPath), "Missing generated dataset: " + datasetPath);
        assertIsolatedDatasource();

        dropCandidateIndexIfPresent();
        deleteDeviceData();
        assertEquals(rows, seedQueryDataset());
        assertEquals(rows, countDevices());

        for (int cycle = 1; cycle <= cycles; cycle++) {
            dropCandidateIndexIfPresent();
            analyzeDevices();
            benchmarkVariant("baseline", cycle);
            capturePlans("baseline", cycle);

            createCandidateIndex(cycle);
            analyzeDevices();
            benchmarkVariant("indexed", cycle);
            capturePlans("indexed", cycle);
            writeReport();
        }

        assertTrue(indexExists(), "Candidate index must remain present after the comparison");
        writeReport();
        assertTrue(Files.size(outputPath) > 0, "Index comparison report was not written");
    }

    private void benchmarkVariant(String variant, int cycle) throws Exception {
        benchmarkRead(variant, cycle, "page-first",
                () -> deviceService.searchDevices(null, null, null, 0, PAGE_SIZE).getNumberOfElements());

        int deepPage = Math.max(0, (rows - 1) / PAGE_SIZE);
        benchmarkRead(variant, cycle, "page-deep",
                () -> deviceService.searchDevices(null, null, null, deepPage, PAGE_SIZE).getNumberOfElements());

        String tailKeyword = String.format(Locale.ROOT, "Scale Device %07d", rows);
        benchmarkRead(variant, cycle, "keyword-tail",
                () -> deviceService.searchDevices(tailKeyword, null, null, 0, PAGE_SIZE).getTotalElements());
    }

    private void benchmarkRead(
            String variant,
            int cycle,
            String operation,
            LongSupplier action) throws Exception {
        for (int warmup = 0; warmup < warmups; warmup++) {
            action.getAsLong();
        }
        for (int run = 1; run <= repetitions; run++) {
            long started = System.nanoTime();
            long units = action.getAsLong();
            double latencyMs = (System.nanoTime() - started) / 1_000_000.0;
            measurements.add(new Measurement(variant, cycle, operation, run, latencyMs, units));
        }
    }

    private void capturePlans(String variant, int cycle) {
        int offset = Math.max(0, (rows - 1) / PAGE_SIZE) * PAGE_SIZE;
        String tailKeyword = String.format(Locale.ROOT, "Scale Device %07d", rows)
                .toLowerCase(Locale.ROOT)
                .replace("'", "''");

        addPlan(variant, cycle, "page-first-data", """
                SELECT id, category, description, model, name, serial_number, state,
                       updated_by, updated_time, version
                FROM devices
                ORDER BY name, id
                LIMIT %d OFFSET 0
                """.formatted(PAGE_SIZE));
        addPlan(variant, cycle, "page-deep-data", """
                SELECT id, category, description, model, name, serial_number, state,
                       updated_by, updated_time, version
                FROM devices
                ORDER BY name, id
                LIMIT %d OFFSET %d
                """.formatted(PAGE_SIZE, offset));
        addPlan(variant, cycle, "page-count", "SELECT COUNT(id) FROM devices");
        addPlan(variant, cycle, "keyword-tail-data", """
                SELECT id, category, description, model, name, serial_number, state,
                       updated_by, updated_time, version
                FROM devices
                WHERE LOWER(name) LIKE '%%%1$s%%'
                   OR LOWER(model) LIKE '%%%1$s%%'
                   OR LOWER(serial_number) LIKE '%%%1$s%%'
                ORDER BY LOWER(name), id
                LIMIT %2$d OFFSET 0
                """.formatted(tailKeyword, PAGE_SIZE));
        addPlan(variant, cycle, "keyword-tail-count", """
                SELECT COUNT(id)
                FROM devices
                WHERE LOWER(name) LIKE '%%%1$s%%'
                   OR LOWER(model) LIKE '%%%1$s%%'
                   OR LOWER(serial_number) LIKE '%%%1$s%%'
                """.formatted(tailKeyword));
    }

    private void addPlan(String variant, int cycle, String name, String sql) {
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
        queryPlans.add(new QueryPlan(variant, cycle, name, sql.strip(), plan));
    }

    private void createCandidateIndex(int cycle) {
        long started = System.nanoTime();
        jdbcTemplate.execute("CREATE INDEX " + INDEX_NAME + " ON devices (" + INDEX_COLUMNS + ")");
        double latencyMs = (System.nanoTime() - started) / 1_000_000.0;
        ddlMeasurements.add(new DdlMeasurement(cycle, latencyMs));
    }

    private void dropCandidateIndexIfPresent() {
        if (indexExists()) {
            jdbcTemplate.execute("DROP INDEX " + INDEX_NAME + " ON devices");
        }
    }

    private boolean indexExists() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'devices'
                  AND index_name = ?
                """, Integer.class, INDEX_NAME);
        return count != null && count > 0;
    }

    private void analyzeDevices() {
        jdbcTemplate.execute("ANALYZE TABLE devices");
    }

    private void assertIsolatedDatasource() {
        String configuredUrl = environment.getProperty("spring.datasource.url", "")
                .toLowerCase(Locale.ROOT);
        assertTrue(configuredUrl.startsWith("jdbc:tc:mysql:"),
                "Index benchmark datasource is not configured as jdbc:tc:mysql");
        assertEquals("device_test", jdbcTemplate.queryForObject("SELECT DATABASE()", String.class));
    }

    private void deleteDeviceData() {
        jdbcTemplate.update("DELETE FROM assignment_extensions");
        jdbcTemplate.update("DELETE FROM device_repairs");
        jdbcTemplate.update("DELETE FROM device_assignments");
        jdbcTemplate.update("DELETE FROM devices");
    }

    private int seedQueryDataset() throws IOException {
        List<DeviceSeed> batch = new ArrayList<>(1_000);
        int count = 0;

        try (Reader reader = Files.newBufferedReader(datasetPath, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {
            for (CSVRecord record : parser) {
                int rowNumber = ++count;
                batch.add(new DeviceSeed(
                        UUID.nameUUIDFromBytes(("device-index-" + rowNumber).getBytes(StandardCharsets.UTF_8))
                                .toString(),
                        record.get("category"),
                        record.get("description"),
                        record.get("model"),
                        record.get("name"),
                        String.format(Locale.ROOT, "IDX%017d", rowNumber)
                ));
                if (batch.size() == 1_000) {
                    insertBatch(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            insertBatch(batch);
        }
        return count;
    }

    private void insertBatch(List<DeviceSeed> batch) {
        StringBuilder sql = new StringBuilder("""
                INSERT INTO devices (
                    id, category, description, model, name, serial_number, state, version
                ) VALUES
                """);
        List<Object> parameters = new ArrayList<>(batch.size() * 6);

        for (int index = 0; index < batch.size(); index++) {
            if (index > 0) {
                sql.append(",\n");
            }
            sql.append("(?, ?, ?, ?, ?, ?, 'AVAILABLE', 0)");

            DeviceSeed seed = batch.get(index);
            parameters.add(seed.id());
            parameters.add(seed.category());
            parameters.add(seed.description());
            parameters.add(seed.model());
            parameters.add(seed.name());
            parameters.add(seed.serialNumber());
        }

        jdbcTemplate.update(sql.toString(), parameters.toArray());
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
        report.append("# Device query index comparison\n\n")
                .append("Generated: ").append(ZonedDateTime.now()).append("\n\n")
                .append("## Environment\n\n")
                .append("| Field | Value |\n|---|---|\n")
                .append("| Base commit | `").append(System.getProperty("device.index-benchmark.commit", "unknown"))
                .append("` |\n")
                .append("| Working tree | ").append(System.getProperty("device.index-benchmark.working-tree", "unknown"))
                .append(" |\n")
                .append("| OS | ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append(' ')
                .append(System.getProperty("os.arch")).append(" |\n")
                .append("| JVM | ").append(System.getProperty("java.vm.name")).append(' ')
                .append(System.getProperty("java.version")).append(" |\n")
                .append("| MySQL | ").append(jdbcTemplate.queryForObject("SELECT VERSION()", String.class))
                .append(" |\n")
                .append("| Datasource | Testcontainers `device_test` (disposable) |\n")
                .append("| Dataset | ").append(rows).append(" synthetic devices from the PRD-208 generator, loaded by multi-row JDBC inserts for query-only timing |\n")
                .append("| Candidate | `CREATE INDEX ").append(INDEX_NAME)
                .append(" ON devices (").append(INDEX_COLUMNS).append(")` |\n")
                .append("| Order | baseline then indexed, repeated for ").append(cycles)
                .append(" alternating cycles |\n")
                .append("| Warm-ups | ").append(warmups).append(" per operation and variant in each cycle |\n")
                .append("| Recorded repetitions | ").append(repetitions)
                .append(" per operation and variant in each cycle |\n\n")
                .append("The B-tree candidate targets deterministic `ORDER BY name, id` for unfiltered pages. Keyword pages ")
                .append("use the collation-equivalent `ORDER BY LOWER(name), id`, preventing MySQL from choosing an expensive ")
                .append("ordered index scan for a leading-wildcard predicate that cannot seek into this B-tree.\n\n")
                .append("## Raw measurements\n\n")
                .append("| variant | cycle | operation | run | latency_ms | units |\n")
                .append("|---|---:|---|---:|---:|---:|\n");

        measurements.forEach(value -> report.append(value.markdownRow()));

        report.append("\n## Summary\n\n")
                .append("| operation | baseline_median_ms | indexed_median_ms | change_percent |\n")
                .append("|---|---:|---:|---:|\n");
        measurements.stream()
                .map(Measurement::operation)
                .distinct()
                .forEach(operation -> appendSummary(report, operation));

        report.append("\n## Index creation on populated table\n\n")
                .append("| cycle | create_index_ms |\n|---:|---:|\n");
        ddlMeasurements.forEach(value -> report.append(String.format(Locale.ROOT,
                "| %d | %.3f |%n", value.cycle(), value.latencyMs())));

        report.append("\n## EXPLAIN ANALYZE\n\n");
        for (QueryPlan plan : queryPlans) {
            report.append("### ").append(plan.variant()).append(" cycle ").append(plan.cycle())
                    .append(" — ").append(plan.name()).append("\n\n")
                    .append("```sql\n").append(plan.sql()).append("\n```\n\n")
                    .append("```text\n").append(plan.plan()).append("\n```\n\n");
        }

        report.append("## Interpretation limits\n\n")
                .append("- Results are local measurements, not a production capacity promise.\n")
                .append("- Both variants use the same process, dataset and MySQL container; alternating cycles reduce but do not eliminate cache/order effects.\n")
                .append("- The comparison measures application `Page` calls, so each latency includes both data and count queries.\n")
                .append("- A normal B-tree cannot seek into a case-folded leading-wildcard search; that operation remains a table scan.\n");

        Files.writeString(outputPath, report.toString(), StandardCharsets.UTF_8);
    }

    private void appendSummary(StringBuilder report, String operation) {
        double baseline = medianLatency("baseline", operation);
        double indexed = medianLatency("indexed", operation);
        double change = baseline == 0 ? 0 : ((baseline - indexed) / baseline) * 100.0;
        report.append(String.format(Locale.ROOT, "| %s | %.3f | %.3f | %+.2f%% |%n",
                operation, baseline, indexed, change));
    }

    private double medianLatency(String variant, String operation) {
        List<Double> values = measurements.stream()
                .filter(value -> value.variant().equals(variant) && value.operation().equals(operation))
                .map(Measurement::latencyMs)
                .sorted(Comparator.naturalOrder())
                .toList();
        int middle = values.size() / 2;
        if (values.size() % 2 == 0) {
            return (values.get(middle - 1) + values.get(middle)) / 2.0;
        }
        return values.get(middle);
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

    private record Measurement(
            String variant,
            int cycle,
            String operation,
            int run,
            double latencyMs,
            long units) {

        String markdownRow() {
            return String.format(Locale.ROOT, "| %s | %d | %s | %d | %.3f | %d |%n",
                    variant, cycle, operation, run, latencyMs, units);
        }
    }

    private record QueryPlan(String variant, int cycle, String name, String sql, String plan) {
    }

    private record DdlMeasurement(int cycle, double latencyMs) {
    }

    private record DeviceSeed(
            String id,
            String category,
            String description,
            String model,
            String name,
            String serialNumber) {
    }

}
