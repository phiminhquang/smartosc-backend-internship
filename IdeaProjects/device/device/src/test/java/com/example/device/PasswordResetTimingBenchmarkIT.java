package com.example.device;

import com.example.device.model.User;
import com.example.device.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.email-job.worker-enabled=false",
        "app.rate-limit.password-reset.ip-limit=10000",
        "app.rate-limit.password-reset.identity-limit=10000",
        "app.scheduler.overdue-cron=0 0 0 1 1 *",
        "app.scheduler.daily-overdue-report-cron=0 0 0 1 1 *"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetTimingBenchmarkIT {

    private static final double MAX_MEDIAN_GAP_MS = 10.0;
    private static final double MAX_MEDIAN_RATIO = 2.0;
    private static final double MAX_DOMINANCE = 0.80;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void compareExistingAndMissingEmailRequestTiming() throws Exception {
        assertEquals("true", System.getProperty("password-reset.timing.enabled"),
                "Run through scripts/benchmark-password-reset-timing.sh with --confirm-isolated");
        assertIsolatedDatasource();

        int samples = positiveInt("password-reset.timing.samples", 50);
        int warmups = nonNegativeInt("password-reset.timing.warmups", 10);
        Path output = requiredOutputPath();
        String runId = UUID.randomUUID().toString();

        List<String> existingEmails = new ArrayList<>();
        for (int index = 0; index < samples + warmups; index++) {
            existingEmails.add("timing-existing-" + runId + "-" + index + "@device.test");
        }
        seedUsers(existingEmails);

        String expectedBody = null;
        for (int index = 0; index < warmups; index++) {
            String missingEmail = "timing-missing-" + runId + "-warmup-" + index + "@device.test";
            expectedBody = runPair(existingEmails.get(index), missingEmail, index, expectedBody, null, null);
        }

        List<Double> existingLatencies = new ArrayList<>();
        List<Double> missingLatencies = new ArrayList<>();
        for (int index = 0; index < samples; index++) {
            String missingEmail = "timing-missing-" + runId + "-sample-" + index + "@device.test";
            expectedBody = runPair(
                    existingEmails.get(warmups + index),
                    missingEmail,
                    index,
                    expectedBody,
                    existingLatencies,
                    missingLatencies
            );
        }

        long createdJobs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM email_jobs WHERE recipient LIKE ?",
                Long.class,
                "timing-existing-" + runId + "%"
        );
        assertEquals(samples + warmups, createdJobs,
                "Only existing synthetic users should create one outbox job each");

        Summary existing = summarize(existingLatencies);
        Summary missing = summarize(missingLatencies);
        double medianGap = Math.abs(existing.medianMs() - missing.medianMs());
        double medianRatio = ratio(existing.medianMs(), missing.medianMs());
        double dominance = dominance(existingLatencies, missingLatencies);
        boolean passed = medianGap <= MAX_MEDIAN_GAP_MS
                && medianRatio <= MAX_MEDIAN_RATIO
                && dominance <= MAX_DOMINANCE;

        writeReport(output, samples, warmups, existing, missing, medianGap, medianRatio, dominance, passed);

        assertTrue(passed, () -> String.format(Locale.ROOT,
                "Clear timing difference detected: median gap %.3f ms, ratio %.3f, dominance %.3f; report: %s",
                medianGap, medianRatio, dominance, output));
    }

    private String runPair(
            String existingEmail,
            String missingEmail,
            int index,
            String expectedBody,
            List<Double> existingLatencies,
            List<Double> missingLatencies) throws Exception {
        Measurement first;
        Measurement second;
        if (index % 2 == 0) {
            first = request(existingEmail);
            second = request(missingEmail);
            add(existingLatencies, first.latencyMs());
            add(missingLatencies, second.latencyMs());
        } else {
            first = request(missingEmail);
            second = request(existingEmail);
            add(missingLatencies, first.latencyMs());
            add(existingLatencies, second.latencyMs());
        }

        String body = expectedBody == null ? first.body() : expectedBody;
        assertEquals(body, first.body(), "Existing and missing responses must remain indistinguishable");
        assertEquals(body, second.body(), "Existing and missing responses must remain indistinguishable");
        return body;
    }

    private Measurement request(String email) throws Exception {
        String json = "{\"email\":\"" + email + "\"}";
        long started = System.nanoTime();
        MvcResult result = mockMvc.perform(post("/api/auth/password-reset/request")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted())
                .andReturn();
        double latencyMs = (System.nanoTime() - started) / 1_000_000.0;
        return new Measurement(latencyMs, result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private void seedUsers(List<String> emails) {
        List<User> users = emails.stream()
                .map(email -> User.builder()
                        .email(email)
                        .name("Timing Benchmark")
                        .password("benchmark-password-hash")
                        .build())
                .toList();
        userRepository.saveAllAndFlush(users);
    }

    private void assertIsolatedDatasource() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertEquals("device_test", database);
        assertTrue(version != null && !version.isBlank());
    }

    private Path requiredOutputPath() throws Exception {
        String raw = System.getProperty("password-reset.timing.output");
        assertTrue(raw != null && !raw.isBlank(), "Missing password-reset.timing.output");
        Path path = Path.of(raw).toAbsolutePath().normalize();
        Files.createDirectories(path.getParent());
        return path;
    }

    private int positiveInt(String property, int fallback) {
        int value = Integer.parseInt(System.getProperty(property, Integer.toString(fallback)));
        assertTrue(value > 0);
        return value;
    }

    private int nonNegativeInt(String property, int fallback) {
        int value = Integer.parseInt(System.getProperty(property, Integer.toString(fallback)));
        assertTrue(value >= 0);
        return value;
    }

    private void add(List<Double> values, double value) {
        if (values != null) {
            values.add(value);
        }
    }

    private Summary summarize(List<Double> values) {
        List<Double> sorted = values.stream().sorted(Comparator.naturalOrder()).toList();
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        return new Summary(
                mean,
                percentile(sorted, 0.50),
                percentile(sorted, 0.95),
                sorted.get(0),
                sorted.get(sorted.size() - 1)
        );
    }

    private double percentile(List<Double> sorted, double percentile) {
        int index = Math.max(0, (int) Math.ceil(percentile * sorted.size()) - 1);
        return sorted.get(index);
    }

    private double ratio(double first, double second) {
        double minimum = Math.min(first, second);
        return minimum == 0.0 ? Double.POSITIVE_INFINITY : Math.max(first, second) / minimum;
    }

    private double dominance(List<Double> existing, List<Double> missing) {
        long existingSlower = 0;
        long comparisons = 0;
        for (double existingValue : existing) {
            for (double missingValue : missing) {
                comparisons++;
                if (existingValue > missingValue) {
                    existingSlower++;
                }
            }
        }
        return (double) existingSlower / comparisons;
    }

    private void writeReport(
            Path output,
            int samples,
            int warmups,
            Summary existing,
            Summary missing,
            double medianGap,
            double medianRatio,
            double dominance,
            boolean passed) throws Exception {
        String mysqlVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        String report = String.format(Locale.ROOT, """
                # Password-reset timing comparison

                | Thuộc tính | Giá trị |
                |---|---|
                | Thời điểm | %s |
                | Commit | `%s` |
                | Working tree | %s |
                | JVM | %s %s |
                | MySQL | %s |
                | Datasource | Testcontainers `device_test` dùng một lần |
                | Phạm vi | MockMvc → controller → shared MySQL rate limit → password-reset service/outbox |
                | Mẫu | %d cặp ghi nhận, %d cặp warm-up; email test riêng cho từng request |
                | Thứ tự | Xen kẽ existing-first/missing-first để giảm drift |

                ## Kết quả

                | Nhánh | Mean (ms) | Median (ms) | P95 (ms) | Min (ms) | Max (ms) |
                |---|---:|---:|---:|---:|---:|
                | Email tồn tại | %.3f | %.3f | %.3f | %.3f | %.3f |
                | Email không tồn tại | %.3f | %.3f | %.3f | %.3f | %.3f |

                - Chênh lệch median tuyệt đối: `%.3f ms` (ngưỡng `<= %.1f ms`).
                - Tỷ lệ median lớn/nhỏ: `%.3f` (ngưỡng `<= %.1f`).
                - Xác suất mẫu existing chậm hơn mẫu missing: `%.3f` (ngưỡng `<= %.2f`).
                - HTTP status và body của hai nhánh giống nhau cho mọi mẫu.
                - Kết luận benchmark: **%s**.

                Ngưỡng nhằm phát hiện chênh lệch rõ ràng trong môi trường local ít nhiễu; không phải
                chứng minh mật mã rằng hai phân phối thời gian hoàn toàn giống nhau. Rate limit production
                vẫn giới hạn số lần lấy mẫu theo IP và normalized identity.
                """,
                ZonedDateTime.now(),
                System.getProperty("password-reset.timing.commit", "unknown"),
                System.getProperty("password-reset.timing.working-tree", "unknown"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.version"),
                mysqlVersion,
                samples,
                warmups,
                existing.meanMs(), existing.medianMs(), existing.p95Ms(), existing.minMs(), existing.maxMs(),
                missing.meanMs(), missing.medianMs(), missing.p95Ms(), missing.minMs(), missing.maxMs(),
                medianGap, MAX_MEDIAN_GAP_MS,
                medianRatio, MAX_MEDIAN_RATIO,
                dominance, MAX_DOMINANCE,
                passed ? "ĐẠT" : "KHÔNG ĐẠT"
        );
        Files.writeString(output, report, StandardCharsets.UTF_8);
    }

    private record Measurement(double latencyMs, String body) {
    }

    private record Summary(double meanMs, double medianMs, double p95Ms, double minMs, double maxMs) {
    }
}
