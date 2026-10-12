package com.example.device.service;

import com.example.device.enums.EmailJobType;

public interface EmailJobSecretCipher {

    EncryptedEmailJobSecret encrypt(EmailJobType jobType, String plaintext);

    String decrypt(
            EmailJobType jobType,
            byte[] ciphertext,
            byte[] nonce,
            String keyVersion
    );
}
