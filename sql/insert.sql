-- Insert script for movies table
INSERT INTO movies (title, genre, duration, release_date, description, director)
VALUES 
('Inception', 'Sci-Fi', 148, '2010-07-16', 'A thief who steals corporate secrets through dream-sharing technology.', 'Christopher Nolan'),
('The Dark Knight', 'Action', 152, '2008-07-18', 'Batman faces the Joker, a criminal mastermind.', 'Christopher Nolan'),
('Interstellar', 'Sci-Fi', 169, '2014-11-07', 'A team of explorers travel through a wormhole in space.', 'Christopher Nolan');

-- Insert script for theaters table
INSERT INTO theaters (name, location, contact_info)
VALUES 
('Downtown Cinema', '123 Main St, Cityville', '123-456-7890'),
('Grand Theater', '456 Elm St, Townsville', '987-654-3210');

-- Insert script for rooms table
INSERT INTO rooms (name, capacity, theater_id)
VALUES 
('Room A', 100, 1),
('Room B', 150, 1),
('Room C', 200, 2);

-- Insert script for showtimes table
INSERT INTO showtimes (movie_id, theater_id, room_id, start_time, end_time, price)
VALUES 
(1, 1, 1, '2023-10-01 14:00:00', '2023-10-01 16:30:00', 12.50),
(2, 1, 2, '2023-10-01 17:00:00', '2023-10-01 19:30:00', 15.00),
(3, 2, 3, '2023-10-01 20:00:00', '2023-10-01 23:00:00', 18.00);

-- Insert script for bookings table
INSERT INTO bookings (user_id, showtime_id, booking_time, status, total_price)
VALUES 
(1, 1, '2023-09-30 10:00:00', 'Confirmed', 25.00),
(2, 2, '2023-09-30 11:00:00', 'Pending', 30.00);

-- Insert script for seats table
INSERT INTO seats (seat_number, is_booked, room_id)
VALUES 
('A1', 0, 1),
('A2', 1, 1),
('B1', 0, 2),
('B2', 1, 2),
('C1', 0, 3),
('C2', 1, 3);

-- Insert script for payments table
INSERT INTO payments (booking_id, user_id, amount, status, payment_method, payment_time)
VALUES 
(1, 1, 25.00, 'Completed', 'Credit Card', '2023-09-30 10:30:00'),
(2, 2, 30.00, 'Pending', 'PayPal', '2023-09-30 11:30:00');