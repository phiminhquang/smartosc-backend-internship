ALTER TABLE email_jobs
    ADD COLUMN request_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL AFTER id;

CREATE INDEX idx_email_jobs_request_id ON email_jobs (request_id);
