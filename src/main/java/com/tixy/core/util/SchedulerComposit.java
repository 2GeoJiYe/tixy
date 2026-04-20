package com.tixy.core.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerComposit {

    private final SchedulerService schedulerService;

    @Scheduled(cron = "0 * * * * *")
    public void updateEventSessionStatus() {
        retryWithBackoff(() -> schedulerService.updateEventSessionStatus(LocalDateTime.now()), "EventSession");
    }

    @Scheduled(cron = "30 * * * * *")
    public void updateTicketTypeStatus() {
        retryWithBackoff(() -> schedulerService.updateTicketTypeStatus(LocalDateTime.now()), "TicketType");
    }

    @Scheduled(cron = "15 */5 * * * *")
    public void updateSeatSessionStatus() {
        retryWithBackoff(() ->schedulerService.updateSeatSessionStatus(LocalDateTime.now()), "SeatSession");
    }

    @Scheduled(cron = "45 0 0 * * *")
    public void updateEventStatus() {
        retryWithBackoff(() -> schedulerService.updateEventStatus(LocalDateTime.now()), "Event");
    }

    private void retryWithBackoff(Runnable task, String name) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                task.run();
                return;
            } catch (Exception e) {
                if (i == maxRetry - 1) {
                    log.error("{} 상태 전이 최종 실패", name, e);
                } else {
                    log.warn("{} 상태 전이 retry {}/{}", name, i + 1, maxRetry);
                    try { Thread.sleep(2000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}