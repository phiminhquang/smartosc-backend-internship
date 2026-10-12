package com.example.device.service.impl;

import com.example.device.configuration.EmailJobSecretProperties;
import com.example.device.enums.EmailJobType;
import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailJobSecretCipher;
import com.example.device.service.EncryptedEmailJobSecret;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailJobSecretCipherImpl implements EmailJobSecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_256_KEY_BYTES = 32;
    private static final int NONCE_BYTES = 12;
    private static final int AUTH_TAG_BITS = 128;

    private final EmailJobSecretProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SecretKeySpec> keys = new LinkedHashMap<>();

    @PostConstruct
    void validateAndLoadKeys() {
        properties.getKeys().forEach((version, encodedKey) -> {
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("Email job secret key version must not be blank");
            }
            if (encodedKey == null || encodedKey.isBlank()) {
                throw new IllegalStateException("Email job secret key must not be blank");
            }
            byte[] decodedKey;
            try {
                decodedKey = Base64.getDecoder().decode(encodedKey);
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Email job secret key must be valid Base64", exception);
            }
            if (decodedKey.length != AES_256_KEY_BYTES) {
                throw new IllegalStateException("Email job secret key must contain exactly 32 bytes");
            }
            keys.put(version, new SecretKeySpec(decodedKey, "AES"));
        });

        if (!keys.containsKey(properties.getActiveKeyVersion())) {
            throw new IllegalStateException("Active email job secret key version is not configured");
        }
    }

    @Override
    public EncryptedEmailJobSecret encrypt(EmailJobType jobType, String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            throw new IllegalArgumentException("Email job secret plaintext is required");
        }

        String keyVersion = properties.getActiveKeyVersion();
        byte[] nonce = new byte[NONCE_BYTES];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keys.get(keyVersion), new GCMParameterSpec(AUTH_TAG_BITS, nonce));
            cipher.updateAAD(jobType.name().getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedEmailJobSecret(ciphertext, nonce, keyVersion);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Cannot encrypt email job secret", exception);
        }
    }

    @Override
    public String decrypt(
            EmailJobType jobType,
            byte[] ciphertext,
            byte[] nonce,
            String keyVersion
    ) {
        SecretKeySpec key = keys.get(keyVersion);
        if (key == null || ciphertext == null || nonce == null || nonce.length != NONCE_BYTES) {
            throw new EmailJobPermanentException("Email job secret cannot be decrypted");
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(AUTH_TAG_BITS, nonce));
            cipher.updateAAD(jobType.name().getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (AEADBadTagException exception) {
            throw new EmailJobPermanentException("Email job secret authentication failed", exception);
        } catch (GeneralSecurityException exception) {
            throw new EmailJobPermanentException("Email job secret cannot be decrypted", exception);
        }
    }
}
