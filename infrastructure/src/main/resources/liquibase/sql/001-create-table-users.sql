CREATE TABLE IF NOT EXISTS users
(
    id    UUID PRIMARY KEY,
    email varchar(254) NOT NULL,
    first_name  varchar(100) NOT NULL,
    last_name  varchar(100) NOT NULL,
    status varchar(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);