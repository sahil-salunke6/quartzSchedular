package com.ss.quartzScheduler.infrastructure.job;

import com.ss.quartzScheduler.domain.entity.JobExecutionMetadata;
import com.ss.quartzScheduler.usecase.SaveJobMetaDataUsaCase;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple Quartz Job that prints "Hello World!" with a timestamp.
 * It includes retry logic
 * Also stores job scheduling metadata in Own created table
 */
@Component
public class HelloWorldJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldJob.class);
    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Execute the job to print "Hello World!" with timestamp
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
            System.out.println("Hello World! (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")");

            // Simulate random failure for retry demonstration
            if (Math.random() < 0.3) {
                throw new RuntimeException("Simulated failure for retry");
            }

            // Reset retry count on success
            dataMap.put(RETRY_COUNT_KEY, 0);

        } catch (Exception e) {
            logger.error("Job execution failed. Retry count: {}", retryCount, e);

            retryCount++;
            dataMap.put(RETRY_COUNT_KEY, retryCount);

            if (retryCount <= MAX_RETRY_ATTEMPTS) {
                logger.info("Scheduling retry attempt {} of {}", retryCount, MAX_RETRY_ATTEMPTS);

                // Create a JobExecutionException with retry flag
                JobExecutionException jobException = new JobExecutionException("Job failed, scheduling retry", e, false);
                jobException.setRefireImmediately(true);
                throw jobException;

            } else {
                logger.error("Job failed after {} attempts. Marking as failed without further retries.", MAX_RETRY_ATTEMPTS);
                dataMap.put(RETRY_COUNT_KEY, 0); // Reset for future executions

                // Don't retry anymore
                throw new JobExecutionException("Job failed after maximum retry attempts", e, false);
            }
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

        SaveJobMetaDataUsaCase.getInstance().saveMetadata(meta);
    }

    /**
     * Format LocalDateTime to String
     *
     * @param dateTime LocalDateTime to format
     * @return formatted date string
     */
    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}