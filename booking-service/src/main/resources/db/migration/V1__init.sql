CREATE TABLE bookings (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL,
    showtime_id      BIGINT         NOT NULL,
    status           VARCHAR(50)    NOT NULL,
    total_price      NUMERIC(12, 2) NOT NULL,
    idempotency_key  VARCHAR(100)   UNIQUE,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0
);

CREATE TABLE booking_seats (
    id           BIGSERIAL PRIMARY KEY,
    booking_id   BIGINT       NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    showtime_id  BIGINT       NOT NULL,
    seat_number  VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_showtime_seat UNIQUE (showtime_id, seat_number)
);

CREATE TABLE outbox_events (
    id            BIGSERIAL PRIMARY KEY,
    destination   VARCHAR(120) NOT NULL,
    aggregate_id  VARCHAR(64)  NOT NULL,
    payload       TEXT         NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    published_at  TIMESTAMP
);

CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_outbox_status ON outbox_events (status, created_at);
