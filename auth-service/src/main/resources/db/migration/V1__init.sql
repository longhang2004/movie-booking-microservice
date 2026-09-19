CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id),
    expires_at   TIMESTAMP   NOT NULL,
    revoked      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
