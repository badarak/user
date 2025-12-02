CREATE TABLE IF NOT EXISTS users
(
    id    UUID PRIMARY KEY,
    email varchar(150) NOT NULL,
    name  varchar(255) NOT NULL
);