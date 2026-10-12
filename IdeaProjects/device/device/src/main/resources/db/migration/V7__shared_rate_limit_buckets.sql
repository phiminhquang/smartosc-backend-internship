CREATE TABLE rate_limit_buckets (
    bucket_key BINARY(32) NOT NULL,
    request_count INT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (bucket_key),
    KEY idx_rate_limit_buckets_expiry (expires_at),
    CONSTRAINT chk_rate_limit_buckets_count CHECK (request_count > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
