package com.playdata.petevent.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 동물 배치 Job 실행용 REST API Controller.
 *
 * POST /api/animals/sync-api 호출 시 수동으로 배치 Job 실행.
 * 배치 중복 실행 방지를 위해 현재시간 기반 JobParameters 전달.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/animals")
public class AnimalsController {

    private final JobLauncher jobLauncher;
    private final Job syncAnimalJob;

    @PostMapping("/sync-api")
    public String runApiSyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(syncAnimalJob, jobParameters);
            return "배치 실행 완료 - 상태: " + execution.getStatus();

        } catch (Exception e) {
            return "배치 실행 실패: " + e.getMessage();
        }
    }
}