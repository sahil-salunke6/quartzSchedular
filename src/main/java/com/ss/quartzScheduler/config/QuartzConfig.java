package com.ss.quartzScheduler.config;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;

/**
 * Quartz configuration class to set up the SchedulerFactoryBean
 * with H2 database for job persistence and retry/recovery capabilities.
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // Persist jobs in H2
        factory.setDataSource(dataSource);
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        // Enable retry/recovery
        factory.setJobFactory((bundle, scheduler) -> {
            Job job;
            try {
                job = bundle.getJobDetail().getJobClass().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return job;
        });

        return factory;
    }

}