package com.ss.quartzScheduler.scheduler;

import com.ss.quartzScheduler.job.HelloWorldJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * JobScheduler initializes and schedules jobs with Quartz on application startup.
 */
@Component
public class JobScheduler implements CommandLineRunner {
    @Autowired
    private Scheduler scheduler;

    @Override
    public void run(String... args) throws Exception {
//        JobDetail jobDetail = JobBuilder.newJob(HelloWorldJob.class)
//                .withIdentity("helloJob", "groupQuartz")
//                .storeDurably()
//                .requestRecovery(true) // Enables replay on failure/restart
//                .build();
//
//        Trigger trigger = TriggerBuilder.newTrigger()
//                .forJob(jobDetail)
//                .withIdentity("helloTrigger", "groupQuartz")
//                .startNow()
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                        .withIntervalInSeconds(5)
//                        .repeatForever())
//                .build();
//
//        // Schedule job with Quartz
//        if (!scheduler.checkExists(jobDetail.getKey())) {
//            scheduler.scheduleJob(jobDetail, trigger);
//        }
    }
}