select count(*) as event_count from tixy.events;

EXPLAIN UPDATE event_sessions
        SET status = 'ON_PERFORM'
        WHERE status = 'SCHEDULED' AND session_open_date <= NOW()
LIMIT 1000;

SELECT trx_id, trx_state, trx_started, trx_query, trx_tables_locked, trx_rows_locked
FROM information_schema.INNODB_TRX;
SELECT trx_mysql_thread_id FROM information_schema.INNODB_TRX WHERE trx_id = 627530;


KILL 3295;


SET FOREIGN_KEY_CHECKS = 0;

-- 원하는 테이블 DROP
DROP TABLE IF EXISTS seat_sessions;
DROP TABLE IF EXISTS seat_sections;
DROP TABLE IF EXISTS ticket_types;
DROP TABLE IF EXISTS event_sessions;
DROP TABLE IF EXISTS events;
-- ... 필요한 것들 다

SET FOREIGN_KEY_CHECKS = 1;