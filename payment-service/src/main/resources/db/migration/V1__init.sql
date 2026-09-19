CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT         NOT NULL UNIQUE,
    user_id             BIGINT         NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    payment_method      VARCHAR(50),
    provider_reference  VARCHAR(255),
    payment_time        TIMESTAMP      NOT NULL
);
