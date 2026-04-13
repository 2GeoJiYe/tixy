ALTER TABLE event_sessions
    ADD COLUMN session_seat_count BIGINT NOT NULL;

ALTER TABLE venues
    ADD COLUMN total_seat_count BIGINT NOT NULL;