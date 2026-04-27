CREATE INDEX idx_event_sessions_status_open_date ON event_sessions (status, session_open_date);

CREATE INDEX idx_event_sessions_status_close_date ON event_sessions (status, session_close_date);

CREATE INDEX idx_ticket_type_status_sale_start ON ticket_types (ticket_type_status, sale_open_date_time);

CREATE INDEX idx_ticket_type_status_sale_end ON ticket_types (ticket_type_status, sale_close_date_time);

CREATE INDEX idx_seat_session_status_hold_expire ON seat_sessions (status, expire_at);