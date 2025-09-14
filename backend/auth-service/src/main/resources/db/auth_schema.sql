create database if not exists auth_db;

CREATE TABLE admin
(
    admin_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL
);