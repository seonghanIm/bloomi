package com.han.bloomi.application.scheduler;

import com.han.bloomi.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 일일 요청 카운트 자동 리셋 스케줄러
 * 매일 자정(00:00)에 모든 사용자의 daily_request_count를 0으로 초기화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyResetScheduler {
    private final UserRepository userRepository;

    /**
     * 매일 자정에 모든 사용자의 일일 요청 카운트를 0으로 리셋합니다.
     * cron: "초 분 시 일 월 요일"
     * "0 0 0 * * *" = 매일 00시 00분 00초
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyRequestCounts() {
        log.info("=== Daily Request Count Reset Job Started ===");

        try {
            userRepository.resetAllDailyRequestCounts();
            log.info("✅ All users' daily request counts have been reset to 0");
        } catch (Exception e) {
            log.error("❌ Failed to reset daily request counts", e);
        }

        log.info("=== Daily Request Count Reset Job Completed ===");
    }
}