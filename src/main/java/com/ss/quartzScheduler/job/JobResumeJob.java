package com.ss.quartzScheduler.job;

import com.ss.quartzScheduler.model.enums.JobStatus;
import com.ss.quartzScheduler.service.DataBaseService;
import com.ss.quartzScheduler.util.CronUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Job class for automatically resuming suspended jobs
 */
@Component
public class JobResumeJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            String originalJobName = dataMap.getString("originalJobName");
            String originalGroupName = dataMap.getString("originalGroupName");

            Scheduler scheduler = context.getScheduler();
            JobKey jobKey = new JobKey(originalJobName, originalGroupName);

            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                System.out.println("Temporary suspended " + originalJobName + " got Resumed at: " + CronUtil.formatDate(LocalDateTime.now()));

                // Update job status in user data
                DataBaseService.getInstance().storeJobUserData(originalJobName, originalGroupName, null, null,
                        null, JobStatus.RESUMED.name());

                // Cancel the auto-resume job itself
                JobKey resumeJobKey = context.getJobDetail().getKey();
                if (scheduler.checkExists(resumeJobKey)) {
                    scheduler.deleteJob(resumeJobKey);
                }
            } else {
                System.out.println("Job not found: " + jobKey);
            }
        } catch (SchedulerException e) {
            throw new JobExecutionException("Failed to resume job", e);
        }
    }
}