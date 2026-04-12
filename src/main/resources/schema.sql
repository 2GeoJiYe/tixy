USE tixy;

CREATE TABLE IF NOT EXISTS venues (
                                      id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      name        VARCHAR(255),
                                      venue_status VARCHAR(50),
                                      location    VARCHAR(50),
                                      created_at  DATETIME,
                                      updated_at  DATETIME
);

CREATE TABLE IF NOT EXISTS events (
                                      id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      venue_id     BIGINT NOT NULL,
                                      title        VARCHAR(255) NOT NULL,
                                      description  TEXT NOT NULL,
                                      category     VARCHAR(50),
                                      event_status VARCHAR(50),
                                      open_date    DATETIME,
                                      end_date     DATETIME,
                                      deleted      TINYINT(1) NOT NULL DEFAULT 0,
                                      deleted_at   DATETIME,
                                      created_at   DATETIME,
                                      updated_at   DATETIME,
                                      FOREIGN KEY (venue_id) REFERENCES venues (id)
);

CREATE TABLE IF NOT EXISTS users (
                                       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       email      VARCHAR(100) NOT NULL UNIQUE,
                                       password   VARCHAR(100),
                                       name       VARCHAR(50)  NOT NULL,
                                       phone      VARCHAR(20)  UNIQUE,
                                       role       VARCHAR(50)  NOT NULL,
                                       created_at DATETIME,
                                       updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS event_sessions (
                                              id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              event_id           BIGINT NOT NULL,
                                              session            VARCHAR(255) NOT NULL,
                                              status             VARCHAR(50),
                                              session_open_date  DATETIME,
                                              session_close_date DATETIME,
                                              created_at         DATETIME,
                                              updated_at         DATETIME,
                                              FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE TABLE IF NOT EXISTS seat_sections (
                                             id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             venue_id     BIGINT NOT NULL,
                                             section_name VARCHAR(255),
                                             grade        VARCHAR(50),
                                             FOREIGN KEY (venue_id) REFERENCES venues (id)
);

CREATE TABLE IF NOT EXISTS seats (
                                     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     seat_section_id BIGINT NOT NULL,
                                     seat_status     VARCHAR(50) NOT NULL,
                                     row_label       VARCHAR(50),
                                     FOREIGN KEY (seat_section_id) REFERENCES seat_sections (id)
);

CREATE TABLE IF NOT EXISTS seat_sessions (
                                             id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             seat_id          BIGINT NOT NULL,
                                             event_session_id BIGINT NOT NULL,
                                             status           VARCHAR(50),
                                             FOREIGN KEY (seat_id) REFERENCES seats (id),
                                             FOREIGN KEY (event_session_id) REFERENCES event_sessions (id)
);

CREATE TABLE IF NOT EXISTS ticket_types (
                                            id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            event_session_id   BIGINT NOT NULL,
                                            seat_section_id    BIGINT NOT NULL,
                                            price              BIGINT NOT NULL,
                                            ticket_type_status VARCHAR(50),
                                            sale_open_date_time  DATETIME NOT NULL,
                                            sale_close_date_time DATETIME NOT NULL,
                                            FOREIGN KEY (event_session_id) REFERENCES event_sessions (id),
                                            FOREIGN KEY (seat_section_id)  REFERENCES seat_sections (id)
);