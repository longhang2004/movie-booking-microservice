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