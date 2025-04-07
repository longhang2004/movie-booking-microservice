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