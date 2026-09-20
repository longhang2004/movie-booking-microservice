DELETE FROM refresh_tokens;

ALTER TABLE refresh_tokens
    ADD COLUMN token_hash VARCHAR(64) NOT NULL,
    ADD COLUMN family_id UUID NOT NULL;

CREATE UNIQUE INDEX uk_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);

ALTER TABLE users
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN locked_until TIMESTAMP NULL;
