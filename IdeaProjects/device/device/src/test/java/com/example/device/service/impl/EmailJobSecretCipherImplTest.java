package com.example.device.service.impl;

import com.example.device.configuration.EmailJobSecretProperties;
import com.example.device.enums.EmailJobType;
import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EncryptedEmailJobSecret;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailJobSecretCipherImplTest {

    @Test
    void aesGcmRoundTripUsesNonceAndDoesNotStorePlaintext() {
        EmailJobSecretCipherImpl cipher = cipher("v1", new byte[32]);
        String rawToken = "raw-password-reset-token";

        EncryptedEmailJobSecret encrypted = cipher.encrypt(EmailJobType.PASSWORD_RESET, rawToken);

        assertEquals(12, encrypted.nonce().length);
        assertFalse(new String(encrypted.ciphertext()).contains(rawToken));
        assertEquals(rawToken, cipher.decrypt(
                EmailJobType.PASSWORD_RESET,
                encrypted.ciphertext(),
                encrypted.nonce(),
                encrypted.keyVersion()
        ));
    }

    @Test
    void tamperedCiphertextFailsClosed() {
        EmailJobSecretCipherImpl cipher = cipher("v1", new byte[32]);
        EncryptedEmailJobSecret encrypted = cipher.encrypt(EmailJobType.PASSWORD_RESET, "raw-token");
        encrypted.ciphertext()[0] ^= 1;

        assertThrows(EmailJobPermanentException.class, () -> cipher.decrypt(
                EmailJobType.PASSWORD_RESET,
                encrypted.ciphertext(),
                encrypted.nonce(),
                encrypted.keyVersion()
        ));
    }

    @Test
    void unknownKeyVersionFailsClosed() {
        EmailJobSecretCipherImpl cipher = cipher("v2", new byte[32]);

        assertThrows(EmailJobPermanentException.class, () -> cipher.decrypt(
                EmailJobType.PASSWORD_RESET,
                new byte[]{1},
                new byte[12],
                "retired-v1"
        ));
    }

    private EmailJobSecretCipherImpl cipher(String version, byte[] key) {
        EmailJobSecretProperties properties = new EmailJobSecretProperties();
        properties.setActiveKeyVersion(version);
        properties.setKeys(Map.of(version, Base64.getEncoder().encodeToString(key)));
        EmailJobSecretCipherImpl cipher = new EmailJobSecretCipherImpl(properties);
        cipher.validateAndLoadKeys();
        return cipher;
    }
}
