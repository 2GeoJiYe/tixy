ALTER TABLE seat_sessions
    ADD COLUMN order_id BIGINT;

ALTER TABLE users
    ADD COLUMN point BIGINT;

CREATE TABLE payments (
                          id                    BIGINT          NOT NULL AUTO_INCREMENT,
                          payment_no            VARCHAR(50)     NOT NULL,
                          ts_hash               VARCHAR(255)    NOT NULL UNIQUE,
                          payment_status        VARCHAR(30),
                          order_id              BIGINT,
                          pay_amount            BIGINT          NOT NULL,
                          pay_value             BIGINT          NOT NULL,
                          sender_wallet_address VARCHAR(255)    NOT NULL,
                          deposit_at            DATETIME        NOT NULL,
                          created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_payments_ts_hash (ts_hash),
                          CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders (id)
);