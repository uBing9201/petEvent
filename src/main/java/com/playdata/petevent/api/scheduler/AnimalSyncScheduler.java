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

/**
 * 유기동물 동기화 Job을 스케줄링하여 정해진 시간마다 자동 실행하는 클래스.
 *
 * 핵심 구성:
 * - JobLauncher: Spring Batch의 Job 실행기
 * - syncAnimalJob: 동기화 대상 배치 잡 (AnimalSyncJobConfig에서 정의됨)
 * - @Scheduled: 스프링 내장 스케줄링 기능을 활용한 주기적 실행 설정
 */
@Component
@RequiredArgsConstructor // 생성자 자동 주입 (final 필드 대상)
@Slf4j // 로그 사용 가능 (Slf4j 기반)
public class AnimalSyncScheduler {

    // Job 실행을 담당하는 Spring Batch의 핵심 컴포넌트
    private final JobLauncher jobLauncher;

    // 실행할 배치 잡 (Job 구성은 AnimalSyncJobConfig에서 정의됨)
    private final Job syncAnimalJob;

    /**
     * 정해진 시간마다 동기화 Job을 자동 실행하는 메서드
     *
     * @Scheduled(cron = "0 0 6-18 * * *")
     * → 매일 6시부터 18시까지 매 정시에 실행됨

     * cron 표현식 의미:
     * "초 분 시 일 월 요일"
     *  → 0초 0분 (정시)에 6~18시 동안 매일 실행
     */
    @Scheduled(cron = "0 0 6-18 * * *") // 하루 6시 ~ 18시 매 정시에 실행
    public void runAnimalSyncJob() {
        try {
            // Job은 매 실행마다 고유한 JobParameters가 필요함
            // → 중복 실행을 방지하기 위해 timestamp를 Job 파라미터에 추가
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 실행 시간 기준 유니크 파라미터
                    .toJobParameters();

            // Job 실행
            JobExecution jobExecution = jobLauncher.run(syncAnimalJob, params);

            // 실행 결과 로그 출력
            log.info("AnimalSync 배치 실행 완료! 상태: {}", jobExecution.getStatus());

        } catch (Exception e) {
            // 실행 중 예외 발생 시 로그로 출력
            log.error("AnimalSync 배치 실행 중 오류 발생", e);
        }
    }
}