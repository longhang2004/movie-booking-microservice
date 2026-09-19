CREATE TABLE showtimes (
    id          BIGSERIAL PRIMARY KEY,
    movie_id    BIGINT         NOT NULL,
    theater_id  BIGINT         NOT NULL,
    room_id     BIGINT         NOT NULL,
    start_time  TIMESTAMP      NOT NULL,
    end_time    TIMESTAMP      NOT NULL,
    price       NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_showtimes_movie ON showtimes (movie_id);
CREATE INDEX idx_showtimes_start ON showtimes (start_time);
