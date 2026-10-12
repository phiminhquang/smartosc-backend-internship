CREATE TABLE email_jobs (
    id BINARY(16) NOT NULL,
    job_type VARCHAR(64) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_key VARCHAR(128) NOT NULL,
    template_version INT NOT NULL,
    payload JSON NOT NULL,
    secret_ciphertext VARBINARY(4096) NULL,
    secret_nonce VARBINARY(12) NULL,
    secret_key_version VARCHAR(64) NULL,
    idempotency_key CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL,
    available_at DATETIME(6) NOT NULL,
    lease_owner VARCHAR(128) NULL,
    lease_until DATETIME(6) NULL,
    last_error_code VARCHAR(64) NULL,
    last_error_summary VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_jobs_idempotency (idempotency_key),
    KEY idx_email_jobs_claim (status, available_at, created_at),
    KEY idx_email_jobs_lease (status, lease_until),
    KEY idx_email_jobs_retention (status, updated_at),
    CONSTRAINT chk_email_jobs_status CHECK (
        status IN ('PENDING', 'RETRY_WAIT', 'PROCESSING', 'SENT', 'DEAD', 'UNKNOWN', 'EXPIRED')
    ),
    CONSTRAINT chk_email_jobs_attempts CHECK (
        attempt_count >= 0 AND max_attempts > 0 AND attempt_count <= max_attempts
    ),
    CONSTRAINT chk_email_jobs_template_version CHECK (template_version > 0),
    CONSTRAINT chk_email_jobs_secret_columns CHECK (
        (secret_ciphertext IS NULL AND secret_nonce IS NULL AND secret_key_version IS NULL)
        OR
        (secret_ciphertext IS NOT NULL AND secret_nonce IS NOT NULL AND secret_key_version IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
