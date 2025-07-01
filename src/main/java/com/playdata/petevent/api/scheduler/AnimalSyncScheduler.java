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

    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행 (e.g., 00:00, 01:00, ...)
    public void runAnimalSyncJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 방지
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(syncAnimalJob, params);
            log.info("AnimalSync 배치 실행 완료! 상태: {}", jobExecution.getStatus());

        } catch (Exception e) {
            log.error("AnimalSync 배치 실행 중 오류 발생", e);
        }
    }
}
