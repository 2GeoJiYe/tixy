CREATE TABLE IF NOT EXISTS tickets (
                                      id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
                                      user_id         BIGINT          NOT NULL,
                                      ticket_type_id  BIGINT          NOT NULL,
                                      seat_session_id BIGINT          NOT NULL UNIQUE,
                                      ticket_status   VARCHAR(20)     NOT NULL,
    used_date_time  DATETIME        NULL,
    issued_date_time DATETIME       NOT NULL,
    CONSTRAINT fk_ticket_member         FOREIGN KEY (user_id)         REFERENCES users(id),
    CONSTRAINT fk_ticket_ticket_type    FOREIGN KEY (ticket_type_id)  REFERENCES ticket_types(id),
    CONSTRAINT fk_ticket_seat_session   FOREIGN KEY (seat_session_id) REFERENCES seat_sessions(id)
    );