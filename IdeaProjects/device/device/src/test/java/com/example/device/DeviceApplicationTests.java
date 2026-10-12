package com.example.device;

import com.example.device.dto.request.PasswordResetConfirmRequest;
import com.example.device.exception.AppException;
import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.EmailJob;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.observability.RequestCorrelation;
import com.example.device.repository.EmailJobRepository;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.repository.UserRepository;
import com.example.device.scheduler.EmailJobWorker;
import com.example.device.service.EmailJobSecretCipher;
import com.example.device.service.PasswordResetService;
import com.example.device.service.impl.RateLimitCounterStore;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Properties;
import java.util.UUID;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DeviceApplicationTests {
	private static final String ISOLATED_JDBC_URL = "jdbc:tc:mysql:8.4:///device_test";
	private static final String TEST_ADMIN_PASSWORD = UUID.randomUUID().toString();
	private static final String TEST_JWT_KEY = Base64.getEncoder()
			.encodeToString(SecureRandom.getSeed(64));

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordResetTokenRepository tokenRepository;
	@Autowired
	private EmailJobRepository emailJobRepository;
	@Autowired
	private PasswordResetService passwordResetService;
	@MockitoSpyBean
	private EmailJobSecretCipher emailJobSecretCipher;
	@Autowired
	private EmailJobWorker emailJobWorker;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RateLimitCounterStore rateLimitCounterStore;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private MeterRegistry meterRegistry;
	@MockitoBean
	private JavaMailSender mailSender;

	@DynamicPropertySource
	static void testSecrets(DynamicPropertyRegistry registry) {
		// Highest-priority test properties prevent an inherited DB_URL from reaching a real database.
		registry.add("spring.datasource.url", () -> ISOLATED_JDBC_URL);
		registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
		registry.add("spring.datasource.username", () -> "");
		registry.add("spring.datasource.password", () -> "");
		registry.add("app.admin.password", () -> TEST_ADMIN_PASSWORD);
		registry.add("jwt.signer-key", () -> TEST_JWT_KEY);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void healthProbesArePublicMinimalAndOtherManagementEndpointsAreProtected() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(header().exists(RequestCorrelation.HEADER_NAME))
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());

		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());

		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());

		mockMvc.perform(get("/actuator"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isUnauthorized());

		assertFalse(meterRegistry.find("http.server.requests").timers().isEmpty());
		assertNotNull(meterRegistry.find("jdbc.connections.active").gauge());
		assertEquals(7, meterRegistry.find("device.email.jobs").gauges().size());
	}

	@Test
	void flywayAppliesAllMigrationsAndDeviceNameIndexToDisposableMySql() {
		Integer appliedMigrations = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class);
		assertEquals(8, appliedMigrations);

		Integer deviceNameIndexColumns = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				  AND table_name = 'devices'
				  AND index_name = 'idx_devices_name'
				  AND column_name = 'name'
				""", Integer.class);
		assertEquals(1, deviceNameIndexColumns);

		Integer emailJobClaimIndexColumns = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				  AND table_name = 'email_jobs'
				  AND index_name = 'idx_email_jobs_claim'
				""", Integer.class);
		assertEquals(3, emailJobClaimIndexColumns);

		Integer emailJobIdempotencyUniqueIndex = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				  AND table_name = 'email_jobs'
				  AND index_name = 'uk_email_jobs_idempotency'
				  AND non_unique = 0
				""", Integer.class);
		assertEquals(1, emailJobIdempotencyUniqueIndex);

		Integer emailJobRequestIdIndex = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				  AND table_name = 'email_jobs'
				  AND index_name = 'idx_email_jobs_request_id'
				  AND column_name = 'request_id'
				""", Integer.class);
		assertEquals(1, emailJobRequestIdIndex);

		Integer passwordResetMetadataConstraint = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.table_constraints
				WHERE constraint_schema = DATABASE()
				  AND table_name = 'email_jobs'
				  AND constraint_name = 'chk_email_jobs_password_reset_metadata'
				""", Integer.class);
		assertEquals(1, passwordResetMetadataConstraint);

		Integer assignmentMetadataConstraint = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.table_constraints
				WHERE constraint_schema = DATABASE()
				  AND table_name = 'email_jobs'
				  AND constraint_name = 'chk_email_jobs_assignment_metadata'
				""", Integer.class);
		assertEquals(1, assignmentMetadataConstraint);

		Integer rateLimitExpiryIndexColumns = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.statistics
				WHERE table_schema = DATABASE()
				  AND table_name = 'rate_limit_buckets'
				  AND index_name = 'idx_rate_limit_buckets_expiry'
				  AND column_name = 'expires_at'
				""", Integer.class);
		assertEquals(1, rateLimitExpiryIndexColumns);
	}

	@Test
	void sharedRateLimitCounterIncrementsAtomicallyAcrossConcurrentTransactions() throws Exception {
		byte[] key = MessageDigest.getInstance("SHA-256")
				.digest(("rate-limit-" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
		Instant now = Instant.now();
		int callers = 20;
		ExecutorService executor = Executors.newFixedThreadPool(callers);
		CountDownLatch ready = new CountDownLatch(callers);
		CountDownLatch start = new CountDownLatch(1);

		try {
			Set<Future<Integer>> futures = new HashSet<>();
			for (int index = 0; index < callers; index++) {
				futures.add(executor.submit(() -> {
					ready.countDown();
					start.await(10, TimeUnit.SECONDS);
					return rateLimitCounterStore.incrementAndGet(key, now, now.plusSeconds(300));
				}));
			}
			assertTrue(ready.await(10, TimeUnit.SECONDS));
			start.countDown();

			Set<Integer> observedCounts = new HashSet<>();
			for (Future<Integer> future : futures) {
				observedCounts.add(future.get(20, TimeUnit.SECONDS));
			}

			assertEquals(callers, observedCounts.size());
			assertTrue(observedCounts.contains(1));
			assertTrue(observedCounts.contains(callers));
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void passwordResetUsesDisposableMySqlAndConsumesTokenOnce() throws Exception {
		String email = "reset-" + UUID.randomUUID() + "@device.test";
		String oldPassword = UUID.randomUUID().toString();
		String newPassword = UUID.randomUUID().toString();
		User user = userRepository.saveAndFlush(User.builder()
				.email(email)
				.name("Integration Test")
				.password(passwordEncoder.encode(oldPassword))
				.build());

		try (RequestCorrelation.Scope ignored = RequestCorrelation.open("request-password-reset-integration")) {
			passwordResetService.requestReset(email);
		}

		EmailJob emailJob = emailJobRepository.findAll().stream()
				.filter(candidate -> candidate.getJobType() == EmailJobType.PASSWORD_RESET)
				.filter(candidate -> candidate.getRecipient().equals(email))
				.findFirst()
				.orElseThrow();
		assertEquals(EmailJobStatus.PENDING, emailJob.getStatus());
		assertEquals("request-password-reset-integration", emailJob.getRequestId());
		assertEquals(4, emailJob.getMaxAttempts());
		assertNotNull(emailJob.getPasswordResetTokenId());
		assertNotNull(emailJob.getExpiresAt());
		assertFalse(emailJob.getPayload().contains("token"));

		String rawToken = emailJobSecretCipher.decrypt(
				emailJob.getJobType(),
				emailJob.getSecretCiphertext(),
				emailJob.getSecretNonce(),
				emailJob.getSecretKeyVersion()
		);
		assertFalse(rawToken.isBlank());
		String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(rawToken.getBytes(StandardCharsets.UTF_8)));

		PasswordResetToken token = tokenRepository.findAll().stream()
				.filter(candidate -> candidate.getTokenHash().equals(expectedHash))
				.findFirst()
				.orElseThrow();
		assertEquals(64, expectedHash.length());
		assertNotEquals(rawToken, token.getTokenHash());
		assertEquals(token.getId(), emailJob.getPasswordResetTokenId());
		assertEquals(token.getExpiresAt(), emailJob.getExpiresAt());

		when(mailSender.createMimeMessage()).thenAnswer(invocation ->
				new MimeMessage(Session.getInstance(new Properties())));
		assertEquals(1, emailJobWorker.processBatch());
		EmailJob sentJob = emailJobRepository.findById(emailJob.getId()).orElseThrow();
		assertEquals(EmailJobStatus.SENT, sentJob.getStatus());
		assertNull(sentJob.getSecretCiphertext());
		assertNull(sentJob.getSecretNonce());
		assertNull(sentJob.getSecretKeyVersion());
		ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(messageCaptor.capture());
		assertTrue(messageCaptor.getValue().getContent().toString().contains(rawToken));

		PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
		request.setToken(rawToken);
		request.setNewPassword(newPassword);
		passwordResetService.confirmReset(request);

		User updatedUser = userRepository.findById(user.getId()).orElseThrow();
		assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
		assertEquals(1, updatedUser.getTokenVersion());
		assertNotNull(tokenRepository.findById(token.getId()).orElseThrow().getUsedAt());
		assertThrows(AppException.class, () -> passwordResetService.confirmReset(request));
	}

	@Test
	void passwordResetTokenAndEmailJobRollBackTogetherWhenEnqueueFails() {
		String email = "rollback-" + UUID.randomUUID() + "@device.test";
		userRepository.saveAndFlush(User.builder()
				.email(email)
				.name("Rollback Test")
				.password("password-hash")
				.build());
		long tokenCountBefore = tokenRepository.count();
		long jobCountBefore = emailJobRepository.count();
		doThrow(new IllegalStateException("simulated encryption failure"))
				.when(emailJobSecretCipher)
				.encrypt(any(), any());

		assertThrows(IllegalStateException.class, () -> passwordResetService.requestReset(email));

		assertEquals(tokenCountBefore, tokenRepository.count());
		assertEquals(jobCountBefore, emailJobRepository.count());
	}
}
