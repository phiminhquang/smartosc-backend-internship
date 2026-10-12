ALTER TABLE email_jobs
    ADD COLUMN assignment_id BINARY(16) NULL AFTER password_reset_token_id,
    ADD COLUMN assignment_expected_return_at DATETIME(6) NULL AFTER assignment_id,
    ADD KEY idx_email_jobs_assignment (assignment_id),
    ADD CONSTRAINT fk_email_jobs_assignment
        FOREIGN KEY (assignment_id) REFERENCES device_assignments (id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_email_jobs_assignment_metadata CHECK (
        (job_type IN ('ASSIGNMENT_REMINDER', 'ASSIGNMENT_OVERDUE')
            AND assignment_id IS NOT NULL
            AND assignment_expected_return_at IS NOT NULL)
        OR
        (job_type NOT IN ('ASSIGNMENT_REMINDER', 'ASSIGNMENT_OVERDUE')
            AND assignment_id IS NULL
            AND assignment_expected_return_at IS NULL)
    );
