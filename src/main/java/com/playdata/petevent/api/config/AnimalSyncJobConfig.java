package com.playdata.petevent.api.config;

import com.playdata.petevent.api.entity.AnimalsEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class AnimalSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final AnimalApiItemReader reader;
    private final AnimalProcessor processor;

    @Bean
    public JpaItemWriter<AnimalsEntity> animalWriter() {
        JpaItemWriter<AnimalsEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step apiToDbStep() {
        return new StepBuilder("apiToDbStep", jobRepository)
                .<AnimalsEntity, AnimalsEntity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(animalWriter())
                .build();
    }

    @Bean
    public Job syncAnimalJob() {
        return new JobBuilder("syncAnimalJob", jobRepository)
                .start(apiToDbStep())
                .build();
    }
}