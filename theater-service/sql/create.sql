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