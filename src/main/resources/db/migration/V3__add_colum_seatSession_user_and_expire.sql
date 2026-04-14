ALTER TABLE seat_sessions
    ADD COLUMN user_id BIGINT,
    ADD COLUMN expire_at DATETIME;

ALTER TABLE seats
    ADD COLUMN created_at DATETIME NOT NULL,
    ADD COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE seat_sections
    ADD COLUMN created_at DATETIME NOT NULL,
    ADD COLUMN updated_at DATETIME NOT NULL;