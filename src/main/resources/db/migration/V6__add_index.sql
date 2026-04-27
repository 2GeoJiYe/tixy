CREATE INDEX idx_events ON events (category, event_status, open_date, end_date);

CREATE INDEX idx_ticket_type ON ticket_types (event_session_id, ticket_type_status, price);

CREATE INDEX idx_seat_sessions_event_session_seat ON seat_sessions (event_session_id, seat_id);