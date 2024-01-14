package com.example.springbatchtutorial.job.DbDataReadWrite;

import com.example.springbatchtutorial.SpringBatchTestConfig;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.accounts.AccountsRepository;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.orders.Orders;
import com.example.springbatchtutorial.job.DbDataReadWrite.core.domain.orders.OrdersRepository;
import com.example.springbatchtutorial.job.HelloWorld.HelloWorldJobConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBatchTest
//@SpringJUnitConfig(TrMigrationConfig.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {SpringBatchTestConfig.class, TrMigrationConfig.class})
class TrMigrationConfigTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @AfterEach
    public void cleanUpEach() {
        ordersRepository.deleteAll();
        accountsRepository.deleteAll();
    }

    @Test
    public void success_noData() throws Exception {
//        this.jobLauncherTestUtils.setJob(trMigrationJob);

        // when
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(accountsRepository.count()).isEqualTo(0);
    }

    @Test
    public void success_existData() throws Exception {
        // given
        Orders orders1 = new Orders(null, "kakao gift", 15000, new Date());
        Orders orders2 = new Orders(null, "naver gift", 15000, new Date());
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);

        // when
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(accountsRepository.count()).isEqualTo(2);
    }
}