package com.ss.quartzScheduler.job;

import com.ss.quartzScheduler.model.enums.JobStatus;
import com.ss.quartzScheduler.service.DataBaseService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A Quartz Job that mimics ADR processing logic.
 * It includes retry logic
 * Also stores job scheduling metadata in Own created table
 */
@Component
@DisallowConcurrentExecution // Prevents concurrent execution of the same job
@PersistJobDataAfterExecution // Persists job data after execution
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

        LocalDateTime nextFireTime = context.getNextFireTime() != null ?
                context.getNextFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;

        try {
            // Store job scheduling metadata
            DataBaseService.getInstance().storeJobMetadata(context);

            // Execute the actual job logic
            demoJob();

            // Simulate random failure to test retry functionality
//            if (Math.random() < 0.3) {
//                throw new RuntimeException("Simulated failure for retry");
//            }

            // Update job user data in database
            DataBaseService.getInstance().storeJobUserData(context.getJobDetail().getKey().getName(),
                    context.getJobDetail().getKey().getGroup(),
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
                DataBaseService.getInstance().storeJobUserData(context.getJobDetail().getKey().getName(),
                        context.getJobDetail().getKey().getGroup(),
                        context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        null,
                        nextFireTime,
                        JobStatus.FAILED.name());

                // Create a JobExecutionException with retry flag
                JobExecutionException jobException = new JobExecutionException("Job failed, scheduling retry", e,
                        false);
                jobException.setRefireImmediately(true);
                throw jobException;

            } else {
                logger.error("Job failed after {} attempts. Marking as failed without further retries.",
                        MAX_RETRY_ATTEMPTS);
                dataMap.put(RETRY_COUNT_KEY, 0); // Reset for future executions

                // Update job user data in database
                DataBaseService.getInstance().storeJobUserData(context.getJobDetail().getKey().getName(),
                        context.getJobDetail().getKey().getGroup(),
                        context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        null,
                        nextFireTime,
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
            System.out.println("Setp No 2: **** CUSIP No : #12345. Validation of Data completed. Going for Processing" +
                    ". ****");
            Thread.sleep(1000);
            System.out.println("Setp No 3: **** CUSIP No : #12345. Processing The Data from DSF and Announcement ****");
            Thread.sleep(3000);
            System.out.println("Setp No 4: **** Creating Email [Announcement-Pending-Email] for CUSIP #12345 and " +
                    "SEC_ID = ANB ****");
            Thread.sleep(2000);
            System.out.println("Setp No 5_A: **** Sending the Email to adr.admin@citi.com and adr.accountmanager@citi" +
                    ".com ****");
            Thread.sleep(2000);
            System.out.println("Setp No 5_B: **** Cusip No : 12345. Connected to DMC, Inserted the Email to DMC " +
                    "system. DMC-ID = DMC123PQR ****");
            Thread.sleep(2000);
            System.out.println("Setp No 7: **** Cusip No : #12345. Updating Tables DSF Table, ADR.Announcement, ADR" +
                    ".Maker_Checker, ADR.AUDIT Table, ADR.DREAM EMAIL, META.Email");

            System.out.println("******** Processing for Cusip No : 12345 Completed in 12 seconds ********");
        } catch (InterruptedException ex) {
            logger.error("Exception : " + ex.getMessage());
        }
    }

}