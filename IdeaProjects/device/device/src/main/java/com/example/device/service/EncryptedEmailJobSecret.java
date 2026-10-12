package com.example.device.service;

public record EncryptedEmailJobSecret(
        byte[] ciphertext,
        byte[] nonce,
        String keyVersion
) {
}
