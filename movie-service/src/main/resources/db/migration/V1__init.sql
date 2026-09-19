CREATE TABLE movies (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    genre         VARCHAR(100) NOT NULL,
    duration      INTEGER      NOT NULL,
    release_date  DATE         NOT NULL,
    description   TEXT,
    director      VARCHAR(255) NOT NULL
);

CREATE INDEX idx_movies_genre ON movies (genre);
CREATE INDEX idx_movies_title ON movies (title);
