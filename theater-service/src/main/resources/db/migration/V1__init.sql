CREATE TABLE theaters (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL,
    contact_info  VARCHAR(255)
);

CREATE TABLE rooms (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    capacity    INTEGER      NOT NULL,
    theater_id  BIGINT       NOT NULL REFERENCES theaters (id) ON DELETE CASCADE
);

CREATE INDEX idx_rooms_theater_id ON rooms (theater_id);
