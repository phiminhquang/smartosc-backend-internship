package com.example.device;

import com.example.device.dto.request.PasswordResetConfirmRequest;
import com.example.device.exception.AppException;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import com.example.device.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
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
	private PasswordResetService passwordResetService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@MockitoBean
	private EmailService emailService;

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
	void flywayAppliesBothMigrationsToDisposableMySql() {
		Integer appliedMigrations = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class);
		assertEquals(2, appliedMigrations);
	}

	@Test
	void passwordResetUsesDisposableMySqlAndConsumesTokenOnce() throws NoSuchAlgorithmException {
		String email = "reset-" + UUID.randomUUID() + "@device.test";
		String oldPassword = UUID.randomUUID().toString();
		String newPassword = UUID.randomUUID().toString();
		User user = userRepository.saveAndFlush(User.builder()
				.email(email)
				.name("Integration Test")
				.password(passwordEncoder.encode(oldPassword))
				.build());

		passwordResetService.requestReset(email);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
		verify(emailService).sendHtmlEmail(eq(email), anyString(), eq("email/password-reset"), variables.capture());
		String resetLink = (String) variables.getValue().get("resetLink");
		String rawToken = URI.create(resetLink).getRawQuery().substring("token=".length());
		assertFalse(rawToken.isBlank());
		String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(rawToken.getBytes(StandardCharsets.UTF_8)));

		PasswordResetToken token = tokenRepository.findAll().stream()
				.filter(candidate -> candidate.getTokenHash().equals(expectedHash))
				.findFirst()
				.orElseThrow();
		assertEquals(64, expectedHash.length());
		assertNotEquals(rawToken, token.getTokenHash());

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
}
