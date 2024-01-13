package com.example.springbatchtutorial.job.DbDataReadWrite;

import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.accounts.Accounts;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.accounts.AccountsRepository;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.orders.Orders;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.Collections;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run: --spring.batch.job.name=trMigrationJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {
    private final OrdersRepository ordersRepository;
    private final AccountsRepository accountsRepository;

    @Bean
    public Job trMigrationJob(JobRepository jobRepository, Step trMigrationStep) {
        return new JobBuilder("trMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(trMigrationStep)
                .build();
    }

    @JobScope
    @Bean
    public Step trMigrationStep(JobRepository jobRepository, Tasklet jobListenerTasklet,
                                PlatformTransactionManager platformTransactionManager,
                                ItemReader trOrdersReader,
                                ItemProcessor trOrderProcessor,
                                ItemWriter trOrdersWriter) {
        return new StepBuilder("trMigrationStep", jobRepository)
                .<Orders, Accounts>chunk(5, platformTransactionManager)
                .reader(trOrdersReader)
//                .writer(new ItemWriter() {
//                    @Override
//                    public void write(final Chunk chunk) throws Exception {
//                        chunk.forEach(System.out::println);
//                    }
//                })
                .processor(trOrderProcessor)
                .writer(trOrdersWriter)
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemWriter<Accounts> trOrdersWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository)
                .methodName("save")
                .build();
    }

    @StepScope
    @Bean
    public ItemWriter<Accounts> trOrdersWriter2() {
        return new ItemWriter<Accounts>() {
            @Override
            public void write(final Chunk<? extends Accounts> chunk) throws Exception {
                chunk.forEach(item -> {
                    accountsRepository.save(item);
                });
            }
        };
    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(final Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                .repository(ordersRepository)
                .methodName("findAll")
                .pageSize(5)
                .arguments(Arrays.asList()) // 메소드의 아규먼트, 현재는 없어서 비어있음
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }
}
