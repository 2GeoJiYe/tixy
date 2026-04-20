# scheduler 에서 대량의 데이터를 업데이트 할 때 시간이 너무 오래 소요되어 비관적 락 발생
# 업데이트에 소요되는 시간을 최소화 하기 위해 추가 IDX 도입

CREATE INDEX idx_event_sessions_status_open_date ON event_sessions (status, session_open_date);

CREATE INDEX idx_event_sessions_status_close_date ON event_sessions (status, session_close_date);

CREATE INDEX idx_ticket_type_status_sale_start ON ticket_types (ticket_type_status, sale_open_date_time);

CREATE INDEX idx_ticket_type_status_sale_end ON ticket_types (ticket_type_status, sale_close_date_time);

CREATE INDEX idx_seat_session_status_hold_expire ON seat_sessions (status, expire_at);