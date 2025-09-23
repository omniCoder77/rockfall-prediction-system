create
database if not exists auth_db;

CREATE TABLE admin
(
    admin_id     UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name         VARCHAR(255)        NOT NULL,
    phone_number VARCHAR(50)         NOT NULL,
    email        VARCHAR(255) UNIQUE NOT NULL,
    password     VARCHAR(255)        NOT NULL,
    job_id       VARCHAR(100)        NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);