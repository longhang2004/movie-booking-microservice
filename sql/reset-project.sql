-- filepath: c:\Users\hi\Documents\Non-Backup\movie-booking-microservice\sql\reset-project.sql

-- Drop tables in reverse order of dependencies to avoid foreign key constraint issues

-- Booking Service
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS bookings;

-- Showtime Service
DROP TABLE IF EXISTS showtimes;

-- Theater Service
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS theaters;

-- Payment Service
DROP TABLE IF EXISTS payments;

-- Movie Service
DROP TABLE IF EXISTS movies;