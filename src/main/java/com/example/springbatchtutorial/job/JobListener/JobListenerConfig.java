package com.example.springbatchtutorial.job.JobListener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JobListenerConfig {
    @Bean
    public Job jobListenerJob(JobRepository jobRepository, Step jobListenerStep) {
        return new JobBuilder("jobListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobLoggerListener())
                .start(jobListenerStep)
                .build();
    }

    @JobScope
    @Bean
    public Step jobListenerStep(JobRepository jobRepository, Tasklet jobListenerTasklet, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("jobListenerStep", jobRepository)
                .tasklet(jobListenerTasklet, platformTransactionManager).build();
    }

    @StepScope
    @Bean
    public Tasklet jobListenerTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
                System.out.println("Job Listener Tasklet");
                throw new Exception("Failed");
//                return RepeatStatus.FINISHED;
            }
        };
    }
}
