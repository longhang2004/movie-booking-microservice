CREATE TABLE showtimes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    movie_id BIGINT NOT NULL,  -- Liên kết với bảng movies
    theater_id BIGINT NOT NULL, -- Liên kết với bảng theaters
    room_id BIGINT NOT NULL,    -- Liên kết với bảng rooms
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (theater_id) REFERENCES theaters(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);