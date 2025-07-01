package com.playdata.petevent.api.config;

import com.playdata.petevent.api.batch.processor.AnimalProcessor;
import com.playdata.petevent.api.batch.reader.AnimalApiItemReader;
import com.playdata.petevent.api.batch.writer.AnimalCustomItemWriter;
import com.playdata.petevent.api.entity.AnimalsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch 배치 잡(Job)과 스텝(Step)을 설정하는 구성 클래스.

 * 이 클래스는 유기동물 보호소 공공 API로부터 데이터를 수집하여,
 * 내부 DB에 적재하고, 불필요한 데이터(더 이상 API에 없는 데이터)를 삭제하는
 * 전체 배치 흐름을 정의합니다.

 * 구성 요소:
 * - AnimalApiItemReader: 외부 API에서 동물 데이터를 읽어옴
 * - AnimalProcessor: 읽은 데이터를 가공 (현재는 PassThrough)
 * - AnimalCustomItemWriter: DB에 insert 또는 update
 * - AnimalStepListener: 스텝 실행 후 정리 작업 (불필요한 데이터 삭제)

 * 이 구성은 Spring Batch에서 가장 흔한 형태인 Chunk 기반 처리 모델을 사용합니다.
 */
@Configuration
@RequiredArgsConstructor
public class AnimalSyncJobConfig {

    // Spring Batch의 내부 실행 상태 저장소
    private final JobRepository jobRepository;

    // 트랜잭션 처리를 위한 매니저 (보통 JPA 트랜잭션 매니저)
    private final PlatformTransactionManager transactionManager;

    // 동물 API에서 데이터를 읽는 ItemReader
    private final AnimalApiItemReader reader;

    // 읽어온 데이터를 처리 (현재는 그대로 반환하는 Processor)
    private final AnimalProcessor processor;

    // DB에 데이터를 저장/업데이트하는 ItemWriter
    private final AnimalCustomItemWriter writer;

    // Step 종료 후 불필요한 데이터를 삭제하는 Listener
    private final AnimalStepListener listener;

    /**
     * Step 정의 - 'apiToDbStep'
     * - 기능: API에서 데이터를 읽고, 가공하고, DB에 저장
     * - 처리 단위(Chunk size): 300개씩 트랜잭션으로 묶어 처리
     */
    @Bean
    public Step apiToDbStep() {
        return new StepBuilder("apiToDbStep", jobRepository)
                // <Input 타입, Output 타입> 설정
                .<AnimalsEntity, AnimalsEntity>chunk(300, transactionManager)
                // Reader: API에서 읽기
                .reader(reader)
                // Processor: 가공 (현재는 그대로 반환)
                .processor(processor)
                // Writer: DB에 저장 또는 업데이트
                .writer(writer)
                // Step 실행 후 API에 없어진 데이터 삭제 처리
                .listener(listener)
                .build();
    }

    /**
     * Job 정의 - 'syncAnimalJob'
     * - 하나의 Step(apiToDbStep)을 순차적으로 실행하는 단일 Step Job 구성
     * - Job 실행 시 자동으로 Step이 시작됨
     */
    @Bean
    public Job syncAnimalJob() {
        return new JobBuilder("syncAnimalJob", jobRepository)
                .start(apiToDbStep()) // 시작 Step 지정
                .build();
    }
}