-- Select all movies
SELECT * FROM movies;

-- Select all theaters
SELECT * FROM theaters;

-- Select all rooms
SELECT * FROM rooms;

-- Select all showtimes
SELECT * FROM showtimes;

-- Select all bookings
SELECT * FROM bookings;

-- Select all seats
SELECT * FROM seats;

-- Select all payments
SELECT * FROM payments;

-- Join query to get showtimes with movie and theater details
SELECT 
    s.id AS showtime_id,
    m.title AS movie_title,
    t.name AS theater_name,
    r.name AS room_name,
    s.start_time,
    s.end_time,
    s.price
FROM showtimes s
JOIN movies m ON s.movie_id = m.id
JOIN theaters t ON s.theater_id = t.id
JOIN rooms r ON s.room_id = r.id;

-- Join query to get bookings with user and showtime details
SELECT 
    b.id AS booking_id,
    b.user_id,
    m.title AS movie_title,
    t.name AS theater_name,
    r.name AS room_name,
    s.start_time,
    b.booking_time,
    b.status,
    b.total_price
FROM bookings b
JOIN showtimes s ON b.showtime_id = s.id
JOIN movies m ON s.movie_id = m.id
JOIN theaters t ON s.theater_id = t.id
JOIN rooms r ON s.room_id = r.id;

-- Join query to get payment details with booking information
SELECT 
    p.id AS payment_id,
    p.booking_id,
    p.user_id,
    b.total_price,
    p.amount,
    p.status AS payment_status,
    p.payment_method,
    p.payment_time
FROM payments p
JOIN bookings b ON p.booking_id = b.id;