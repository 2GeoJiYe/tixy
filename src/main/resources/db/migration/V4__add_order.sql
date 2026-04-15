CREATE TABLE IF NOT EXISTS orders (
                                      id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
                                      order_no            VARCHAR(50)     NOT NULL,
    total_price         BIGINT          NOT NULL,
    ticket_count        INT             NOT NULL DEFAULT 0,
    order_status        VARCHAR(20)     NOT NULL,
    user_id             BIGINT          NOT NULL,
    paid_wallet_address VARCHAR(255),
    ticket_type_id      BIGINT          NOT NULL,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL
    );

ALTER TABLE users
    ADD COLUMN wallet_address VARCHAR(255) NULL;