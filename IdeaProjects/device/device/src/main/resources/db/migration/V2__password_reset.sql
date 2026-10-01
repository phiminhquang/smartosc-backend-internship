ALTER TABLE users
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER password;

CREATE TABLE password_reset_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_token_hash (token_hash),
    KEY idx_password_reset_user_created (user_id, created_at),
    KEY idx_password_reset_expires_at (expires_at),
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
