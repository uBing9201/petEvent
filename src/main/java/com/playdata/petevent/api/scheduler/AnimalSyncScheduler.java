package com.playdata.petevent.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnimalSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job syncAnimalJob;

    /**
     * 매 정시(0분)에 자동으로 배치 실행
     * cron 형식: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 5-23 * * *") // 매일 5시 ~ 23시 정시에만 실행
    public void runAnimalSyncJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(syncAnimalJob, params);
            log.info("AnimalSync 배치 실행 완료! 상태: {}", jobExecution.getStatus());

        } catch (Exception e) {
            log.error("AnimalSync 배치 실행 중 오류 발생", e);
        }
    }
}