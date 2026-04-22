package com.tixy.core.metrics;

import com.tixy.api.seat.enums.SessionSeatStatus;
import com.tixy.api.seat.repository.SeatSessionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class TixyMetricsService {

    private static final String SEAT_SESSION_RELEASE_TASK = "seat_session_release";
    private static final long SEAT_SESSION_RELEASE_INTERVAL_SECONDS = 300;

    private final MeterRegistry meterRegistry;
    private final SeatSessionRepository seatSessionRepository;

    private final AtomicLong seatSessionReleaseLastSuccessEpochSeconds = new AtomicLong();

    @PostConstruct
    void registerGauges() {
        Gauge.builder("tixy_seat_sessions_current",
                        seatSessionRepository,
                        repository -> repository.countByStatus(SessionSeatStatus.HELD))
                .description("현재 HELD 상태인 좌석 세션 수")
                .tag("status", SessionSeatStatus.HELD.name())
                .register(meterRegistry);

        Gauge.builder("tixy_scheduler_last_success_epoch_seconds",
                        seatSessionReleaseLastSuccessEpochSeconds,
                        AtomicLong::get)
                .description("좌석 점유 해제 스케줄러가 마지막으로 성공한 시각의 epoch seconds")
                .tag("task", SEAT_SESSION_RELEASE_TASK)
                .register(meterRegistry);

        Gauge.builder("tixy_scheduler_last_success_delay_seconds",
                        seatSessionReleaseLastSuccessEpochSeconds,
                        this::calculateDelaySeconds)
                .description("5분 주기 좌석 점유 해제 스케줄러가 기대 주기를 초과해 지연된 초")
                .tag("task", SEAT_SESSION_RELEASE_TASK)
                .register(meterRegistry);
    }

    public void recordSeatSessionReleaseSuccess(LocalDateTime executedAt) {
        long epochSeconds = executedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        seatSessionReleaseLastSuccessEpochSeconds.set(epochSeconds);
    }

    private double calculateDelaySeconds(AtomicLong lastSuccessEpochSeconds) {
        long lastSuccess = lastSuccessEpochSeconds.get();
        if (lastSuccess <= 0) {
            return 0;
        }

        long now = System.currentTimeMillis() / 1000;
        long elapsedSeconds = Math.max(now - lastSuccess, 0);
        return Math.max(elapsedSeconds - SEAT_SESSION_RELEASE_INTERVAL_SECONDS, 0);
    }
}
