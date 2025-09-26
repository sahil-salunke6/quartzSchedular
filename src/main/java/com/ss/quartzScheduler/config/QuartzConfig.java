package com.ss.quartzScheduler.config;

import com.zaxxer.hikari.HikariDataSource;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Bean
    public DataSource quartzDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:tcp://localhost:9092/mem:quartzdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }

    @Bean
    public JobFactory jobFactory() {
        return new SpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Autowired DataSource quartzDataSource,
                                                     @Autowired JobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(quartzDataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        factory.setSchedulerName("QuartzScheduler");
        return factory;
    }

    @Bean
    public Properties quartzProperties() {
        Properties props = new Properties();

        // Scheduler
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        props.setProperty("org.quartz.scheduler.instanceName", "QuartzScheduler");

        // Thread pool
        props.setProperty("org.quartz.threadPool.threadCount", "5");

        // JobStore
        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        props.setProperty("org.quartz.jobStore.dataSource", "quartzDS");
        props.setProperty("org.quartz.jobStore.isClustered", "true");
        props.setProperty("org.quartz.jobStore.tablePrefix", "DREAM_");

        // Datasource mapping for Quartz
        props.setProperty("org.quartz.dataSource.quartzDS.driver", "org.h2.Driver");
        props.setProperty("org.quartz.dataSource.quartzDS.URL", "jdbc:h2:tcp://localhost:9092/mem:quartzdb;");
        props.setProperty("org.quartz.dataSource.quartzDS.user", "sa");
        props.setProperty("org.quartz.dataSource.quartzDS.password", "sa");
        props.setProperty("org.quartz.dataSource.quartzDS.provider", "hikaricp");

        return props;
    }

    @Bean
    public Scheduler scheduler(@Autowired SchedulerFactoryBean factory) throws Exception {
        return factory.getScheduler();
    }
}
