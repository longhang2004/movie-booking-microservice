CREATE TABLE movies (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    genre NVARCHAR(100),
    duration INT,
    release_date NVARCHAR(50),
    description NVARCHAR(MAX),
    director NVARCHAR(100)
);

CREATE TABLE theaters (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    location NVARCHAR(255),
    contact_info NVARCHAR(255)
);

CREATE TABLE rooms (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    capacity INT,
    theater_id BIGINT,
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

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

CREATE TABLE bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    booking_time DATETIME NOT NULL,
    status NVARCHAR(50) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id)
);

CREATE TABLE seats (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    seat_number NVARCHAR(50) NOT NULL,
    is_booked BIT NOT NULL DEFAULT 0,
    room_id BIGINT,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status NVARCHAR(50) NOT NULL,
    payment_method NVARCHAR(50),
    payment_time DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);