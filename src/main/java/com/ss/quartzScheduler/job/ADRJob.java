package com.ss.quartzScheduler.job;

import com.ss.quartzScheduler.model.entity.JobExecutionMetadata;
import com.ss.quartzScheduler.model.entity.JobUserControl;
import com.ss.quartzScheduler.model.enums.JobStatus;
import com.ss.quartzScheduler.service.DataBaseService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.ss.quartzScheduler.util.CronUtil.*;
import static com.ss.quartzScheduler.util.CronUtil.convertToLocalDateTime;

/**
 * A Quartz Job that mimics ADR processing logic.
 * It includes retry logic
 * Also stores job scheduling metadata in Own created table
 */
@Component
public class ADRJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ADRJob.class);
    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Execute the job to mimic actual job processing logic
     *
     * @param context JobExecutionContext context
     * @throws JobExecutionException in case of job execution failure
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        int retryCount = 0;
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        if (!dataMap.isEmpty()) {
            retryCount = dataMap.getIntValue(RETRY_COUNT_KEY);
        }

        try {
            // Store job scheduling metadata
            storeJobMetadata(context);

            // Execute the actual job logic
            demoJob();

            // Simulate random failure to test retry functionality
//            if (Math.random() < 0.3) {
//                throw new RuntimeException("Simulated failure for retry");
//            }

            LocalDateTime nextFireTime = context.getNextFireTime() != null ?
                    convertToLocalDateTime(context.getNextFireTime()) : null;

            // Update job user data in database
            storeJobUserData(context.getJobDetail().getKey().getName(), context.getJobDetail().getKey().getGroup(),
                    context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    context.getFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    nextFireTime, JobStatus.COMPLETED.name());

            // Reset retry count on success
            dataMap.put(RETRY_COUNT_KEY, 0);

        } catch (Exception e) {
            logger.error("Job execution failed. Retry count: {}", retryCount, e);

            retryCount++;
            dataMap.put(RETRY_COUNT_KEY, retryCount);

            if (retryCount <= MAX_RETRY_ATTEMPTS) {
                logger.info("Scheduling retry attempt {} of {}", retryCount, MAX_RETRY_ATTEMPTS);

                // Update job user data in database
                storeJobUserData(context.getJobDetail().getKey().getName(), context.getJobDetail().getKey().getGroup(),
                        context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        null,
                        null,
                        JobStatus.FAILED.name());

                // Create a JobExecutionException with retry flag
                JobExecutionException jobException = new JobExecutionException("Job failed, scheduling retry", e, false);
                jobException.setRefireImmediately(true);
                throw jobException;

            } else {
                logger.error("Job failed after {} attempts. Marking as failed without further retries.", MAX_RETRY_ATTEMPTS);
                dataMap.put(RETRY_COUNT_KEY, 0); // Reset for future executions

                // Update job user data in database
                storeJobUserData(context.getJobDetail().getKey().getName(), context.getJobDetail().getKey().getGroup(),
                        context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        null,
                        null,
                        JobStatus.FAILED.name());


                // Don't retry anymore
                throw new JobExecutionException("Job failed after maximum retry attempts", e, false);
            }
        }
    }


    /**
     * A demo job method to mimic actual job processing logic
     */
    private void demoJob() {
        try {
            System.out.println("******** Processing for CUSIP NO : 12345 started ********");
            System.out.println("Setp No 1: **** CUSIP No : #12345. Fetched Data from DSF and Announcement Table ****");
            Thread.sleep(2000);
            System.out.println("Setp No 2: **** CUSIP No : #12345. Validation of Data completed. Going for Processing. ****");
            Thread.sleep(1000);
            System.out.println("Setp No 3: **** CUSIP No : #12345. Processing The Data from DSF and Announcement ****");
            Thread.sleep(3000);
            System.out.println("Setp No 4: **** Creating Email [Announcement-Pending-Email] for CUSIP #12345 and SEC_ID = ANB ****");
            Thread.sleep(2000);
            System.out.println("Setp No 5_A: **** Sending the Email to adr.admin@citi.com and adr.accountmanager@citi.com ****");
            Thread.sleep(2000);
            System.out.println("Setp No 5_B: **** Cusip No : 12345. Connected to DMC, Inserted the Email to DMC system. DMC-ID = DMC123PQR ****");
            Thread.sleep(2000);
            System.out.println("Setp No 7: **** Cusip No : #12345. Updating Tables DSF Table, ADR.Announcement, ADR.Maker_Checker, ADR.AUDIT Table, ADR.DREAM EMAIL, META.Email");

            System.out.println("******** Processing for Cusip No : 12345 Completed in 12 seconds ********");
        } catch (InterruptedException ex) {
            logger.error("Exception : " + ex.getMessage());
        }
    }


    /**
     * Store job scheduling metadata in JobDataMap
     *
     * @param context JobExecutionContext context
     */
    private void storeJobMetadata(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // Store scheduling time metadata in QRTZ_JOB_DETAILS as a JOB_DATA in a blob format
        dataMap.put("lastScheduledTime", context.getScheduledFireTime());
        dataMap.put("actualFireTime", context.getFireTime());
        dataMap.put("nextFireTime", context.getNextFireTime());
        dataMap.put("executionTime", LocalDateTime.now());

        // Store scheduling time metadata in QRTZ_JOB_EXECUTION_METADATA table
        JobExecutionMetadata meta = JobExecutionMetadata.builder()
                .jobName(context.getJobDetail().getKey().getName())
                .jobGroup(context.getJobDetail().getKey().getGroup())
                .lastScheduledTime(formatDate(context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .actualFireTime(formatDate(context.getFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .nextFireTime(context.getNextFireTime() != null ?
                        formatDate(context.getNextFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()) : "N/A")
                .executionTime(formatDate(LocalDateTime.now()))
                .build();

        DataBaseService.getInstance().saveMetadata(meta);
    }

    /**
     * Store job user data in the database
     *
     * @param jobName           Job name
     * @param groupName         Job group name
     * @param lastScheduledTime Last scheduled fire time
     * @param actualFireTime    Actual fire time
     * @param nextFireTime      Next scheduled fire time
     * @param status            Job status (e.g., "SCHEDULED", "ACTIVE", "COMPLETED", "FAILED", "SUSPENDED")
     */
    private void storeJobUserData(String jobName, String groupName, LocalDateTime lastScheduledTime,
                                  LocalDateTime actualFireTime, LocalDateTime nextFireTime, String status) {

        try {
            JobUserControl control = DataBaseService.getInstance()
                    .findByJobNameAndJobGroup(jobName, groupName)
                    .orElse(JobUserControl.builder().build());

            control.setJobName(jobName);
            control.setJobGroup(groupName);
            if (lastScheduledTime != null) {
                control.setLastScheduledTime(lastScheduledTime);
            }
            if (actualFireTime != null) {
                control.setActualFireTime(actualFireTime);
            }
            if (nextFireTime != null) {
                control.setNextFireTime(nextFireTime);
            }
            control.setStatus(status);

            DataBaseService.getInstance().saveUserdata(control);

            logger.info("Stored job user data for {}.{} -> status={}", jobName, groupName, status);
        } catch (Exception e) {
            logger.error("Failed to store job user data for {}.{}", jobName, groupName, e);
        }
    }


}