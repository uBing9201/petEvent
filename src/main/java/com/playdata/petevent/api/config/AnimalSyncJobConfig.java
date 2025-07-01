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
 * Spring Batch Job 및 Step 설정 클래스.
 *
 * - AnimalApiItemReader로 API에서 동물 데이터 읽기
 * - AnimalProcessor로 데이터 처리(현재는 그대로 통과)
 * - AnimalCustomItemWriter로 DB에 저장/갱신
 * - AnimalStepListener로 Step 종료 후 삭제 로직 수행
 * - Chunk 단위는 100개
 */
@Configuration
@RequiredArgsConstructor
public class AnimalSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AnimalApiItemReader reader;
    private final AnimalProcessor processor;
    private final AnimalCustomItemWriter writer;
    private final AnimalStepListener listener;

    @Bean
    public Step apiToDbStep() {
        return new StepBuilder("apiToDbStep", jobRepository)
                .<AnimalsEntity, AnimalsEntity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(listener) // Step 종료 후 삭제 처리 리스너 연결
                .build();
    }

    @Bean
    public Job syncAnimalJob() {
        return new JobBuilder("syncAnimalJob", jobRepository)
                .start(apiToDbStep())
                .build();
    }
}