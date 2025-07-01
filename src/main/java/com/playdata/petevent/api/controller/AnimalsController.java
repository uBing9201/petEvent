package com.playdata.petevent.api.controller;

import com.playdata.petevent.api.service.AnimalsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/animals")
public class AnimalsController {

    private final AnimalsSyncService animalsSyncService;

    // 수동 동기화 트리거용 엔드포인트
    @GetMapping("/sync")
    public String syncData() {
        animalsSyncService.syncAbandonedAnimals();
        return "동기화 완료";
    }

    private final JobLauncher jobLauncher;
    private final Job syncAnimalJob;

    @PostMapping("/sync-api")
    public String runApiSyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 방지
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(syncAnimalJob, jobParameters);
            return "배치 실행 완료 - 상태: " + execution.getStatus();
        } catch (Exception e) {
            return "배치 실행 실패: " + e.getMessage();
        }
    }
}