ALTER TABLE email_jobs
    ADD COLUMN password_reset_token_id BINARY(16) NULL AFTER secret_key_version,
    ADD COLUMN expires_at DATETIME(6) NULL AFTER available_at,
    ADD KEY idx_email_jobs_password_reset_token (password_reset_token_id),
    ADD CONSTRAINT fk_email_jobs_password_reset_token
        FOREIGN KEY (password_reset_token_id) REFERENCES password_reset_tokens (id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_email_jobs_password_reset_metadata CHECK (
        (job_type = 'PASSWORD_RESET'
            AND password_reset_token_id IS NOT NULL
            AND expires_at IS NOT NULL)
        OR
        (job_type <> 'PASSWORD_RESET'
            AND password_reset_token_id IS NULL
            AND expires_at IS NULL)
    );
