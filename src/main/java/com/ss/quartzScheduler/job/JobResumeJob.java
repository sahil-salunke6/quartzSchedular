package com.ss.quartzScheduler.job;

import com.ss.quartzScheduler.service.QuartzJobManagementService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * Job class for automatically resuming suspended jobs
 */
@Slf4j
@Component
@ComponentScan(basePackages = "com.ss.quartzScheduler.service")
public class JobResumeJob implements Job {

    @Autowired
    private QuartzJobManagementService service;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String originalJobName = dataMap.getString("originalJobName");
        String originalGroupName = dataMap.getString("originalGroupName");

        try {
            if (service != null) {
                service.revokeSuspension(originalJobName, originalGroupName);
                log.info("Auto-resumed job: {}.{}", originalJobName, originalGroupName);
            }
        } catch (SchedulerException e) {
            log.error("Failed to auto-resume job: {}.{}", originalJobName, originalGroupName, e);
            throw new JobExecutionException(e);
        }
    }
}