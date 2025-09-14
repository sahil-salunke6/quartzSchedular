package com.ss.quartzScheduler.service;

import com.ss.quartzScheduler.job.HelloWorldJob;
import com.ss.quartzScheduler.job.JobResumeJob;
import com.ss.quartzScheduler.model.SuspensionInfo;
import com.ss.quartzScheduler.model.SuspensionType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ss.quartzScheduler.util.CronUtil.CommonCronExpressions.GROUP_NAME;
import static com.ss.quartzScheduler.util.CronUtil.CommonCronExpressions.JOB_NAME;

@Service
public class QuartzJobManagementService {

    private static final Logger logger = LoggerFactory.getLogger(QuartzJobManagementService.class);

    private Scheduler scheduler;
    private final Map<String, SuspensionInfo> suspendedJobs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        logger.info("Quartz Scheduler started successfully");
    }

    @PreDestroy
    public void shutdown() throws SchedulerException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(true);
            logger.info("Quartz Scheduler shut down successfully");
        }
    }

    /**
     * Schedule a job with a CRON expression
     */
    public void scheduleJob(String jobName, String cronExpression) throws SchedulerException {
        try {
            JobKey jobKey = new JobKey(JOB_NAME, GROUP_NAME);

            // Delete existing job if it exists
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                logger.info("Deleted existing job: {}", jobName);
            }

            // Create new job
            JobDetail jobDetail = JobBuilder.newJob(HelloWorldJob.class)
                    .withIdentity(jobKey)
                    .withDescription("Dynamic timestamp job")
                    .storeDurably(true)
                    .requestRecovery(true)
                    .build();

            // Create trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(jobName + "Trigger", GROUP_NAME)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled job: {} with cron: {}", jobName, cronExpression);

        } catch (Exception e) {
            logger.error("Failed to schedule job: {}", jobName, e);
            throw new SchedulerException("Failed to schedule job: " + jobName, e);
        }
    }

    /**
     * Trigger a job immediately
     */
    public void triggerJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);

        if (isJobSuspended(jobName, groupName)) {
            throw new SchedulerException("Cannot trigger suspended job: " + jobName + "." + groupName);
        }

        JobDetail jobDetail = JobBuilder.newJob(HelloWorldJob.class)
                .withIdentity(JOB_NAME, GROUP_NAME)
                .storeDurably()
                .requestRecovery(true) // Enables replay on failure/restart
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobName + "Trigger", GROUP_NAME)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(5)
                        .repeatForever())
                .build();

        // Schedule job with Quartz
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("Job triggered: {}.{}", jobName, groupName);
        } else {
            logger.info("Job already exists: {}.{}", jobName, groupName);
        }

    }

    /**
     * Suspend a job temporarily until a specific date/time
     */
    public void suspendJobTemporary(String jobName, String groupName, LocalDateTime resumeDateTime)
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);

        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job does not exist: " + jobName + "." + groupName);
        }

        // Pause all triggers for this job
        scheduler.pauseJob(jobKey);

        // Store suspension info
        String suspensionKey = jobName + "." + groupName;
        SuspensionInfo suspensionInfo = new SuspensionInfo(
                SuspensionType.TEMPORARY,
                LocalDateTime.now(),
                resumeDateTime,
                "Temporary suspension"
        );
        suspendedJobs.put(suspensionKey, suspensionInfo);

        // Schedule automatic resume
        scheduleJobResume(jobName, groupName, resumeDateTime);

        logger.info("Job suspended temporarily until {}: {}.{}", resumeDateTime, jobName, groupName);
    }

    /**
     * Suspend a job permanently
     */
    public void suspendJobPermanently(String jobName, String groupName, String reason)
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);

        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job does not exist: " + jobName + "." + groupName);
        }

        // Pause all triggers for this job
        scheduler.pauseJob(jobKey);

        // Store suspension info
        String suspensionKey = jobName + "." + groupName;
        SuspensionInfo suspensionInfo = new SuspensionInfo(
                SuspensionType.PERMANENT,
                LocalDateTime.now(),
                null,
                reason != null ? reason : "Permanent suspension"
        );
        suspendedJobs.put(suspensionKey, suspensionInfo);

        logger.info("Job suspended permanently: {}.{}, Reason: {}", jobName, groupName, reason);
    }

    /**
     * Revoke suspension and resume the job
     */
    public void revokeSuspension(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);

        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job does not exist: " + jobName + "." + groupName);
        }

        String suspensionKey = jobName + "." + groupName;
        SuspensionInfo suspensionInfo = suspendedJobs.get(suspensionKey);

        if (suspensionInfo == null) {
            throw new SchedulerException("Job is not suspended: " + jobName + "." + groupName);
        }

        // Resume the job
        scheduler.resumeJob(jobKey);

        // Remove suspension info
        suspendedJobs.remove(suspensionKey);

        // Cancel auto-resume trigger if it exists
        cancelAutoResumeJob(jobName, groupName);

        logger.info("Job suspension revoked: {}.{}", jobName, groupName);
    }

    /**
     * Extend the suspension period for a temporarily suspended job
     */
    public void extendSuspension(String jobName, String groupName, LocalDateTime newResumeDateTime)
            throws SchedulerException {
        String suspensionKey = jobName + "." + groupName;
        SuspensionInfo suspensionInfo = suspendedJobs.get(suspensionKey);

        if (suspensionInfo == null) {
            throw new SchedulerException("Job is not suspended: " + jobName + "." + groupName);
        }

        if (suspensionInfo.getType() != SuspensionType.TEMPORARY) {
            throw new SchedulerException("Cannot extend permanent suspension: " + jobName + "." + groupName);
        }

        // Update suspension info
        suspensionInfo.setResumeDateTime(newResumeDateTime);

        // Cancel existing auto-resume and schedule new one
        cancelAutoResumeJob(jobName, groupName);
        scheduleJobResume(jobName, groupName, newResumeDateTime);

        logger.info("Job suspension extended until {}: {}.{}", newResumeDateTime, jobName, groupName);
    }

    /**
     * Check if a job is currently suspended
     */
    public boolean isJobSuspended(String jobName, String groupName) {
        String suspensionKey = jobName + "." + groupName;
        return suspendedJobs.containsKey(suspensionKey);
    }

    /**
     * Get suspension information for a job
     */
    public SuspensionInfo getSuspensionInfo(String jobName, String groupName) {
        String suspensionKey = jobName + "." + groupName;
        return suspendedJobs.get(suspensionKey);
    }

    /**
     * Get all suspended jobs
     */
    public Map<String, SuspensionInfo> getAllSuspendedJobs() {
        return new HashMap<>(suspendedJobs);
    }

    /**
     * Schedule automatic job resume
     */
    private void scheduleJobResume(String jobName, String groupName, LocalDateTime resumeDateTime)
            throws SchedulerException {
        String resumeJobName = "resume-" + jobName;
        String resumeGroupName = "resume-" + groupName;

        JobDetail resumeJob = JobBuilder.newJob(JobResumeJob.class)
                .withIdentity(resumeJobName, resumeGroupName)
                .usingJobData("originalJobName", jobName)
                .usingJobData("originalGroupName", groupName)
                .build();

        Date resumeDate = Date.from(resumeDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Trigger resumeTrigger = TriggerBuilder.newTrigger()
                .withIdentity("resume-trigger-" + jobName, "resume-trigger-" + groupName)
                .startAt(resumeDate)
                .build();

        scheduler.scheduleJob(resumeJob, resumeTrigger);
    }

    /**
     * Cancel automatic resume job
     */
    private void cancelAutoResumeJob(String jobName, String groupName) throws SchedulerException {
        String resumeJobName = "resume-" + jobName;
        String resumeGroupName = "resume-" + groupName;
        JobKey resumeJobKey = JobKey.jobKey(resumeJobName, resumeGroupName);

        if (scheduler.checkExists(resumeJobKey)) {
            scheduler.deleteJob(resumeJobKey);
        }
    }

}