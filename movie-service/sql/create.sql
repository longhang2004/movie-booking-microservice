CREATE TABLE movies (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    genre NVARCHAR(100),
    duration INT,
    release_date NVARCHAR(50),
    description NVARCHAR(MAX),
    director NVARCHAR(100)
);