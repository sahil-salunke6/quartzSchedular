package com.ss.quartzScheduler.job;

import com.ss.quartzScheduler.service.DataBaseService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.ss.quartzScheduler.util.CronUtil.formatDate;

/**
 * A simple Quartz Job that prints "Hello World!" with a timestamp.
 * It includes retry logic
 * Also stores job scheduling metadata in Own created table
 */
@Component
@DisallowConcurrentExecution // Prevents concurrent execution of the same job
@PersistJobDataAfterExecution // Persists job data after execution
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
            DataBaseService.getInstance().storeJobMetadata(context);

            // Execute the actual job logic
            System.out.println("Hello World! (" + formatDate(LocalDateTime.now()) + ")");

            // Simulate random failure to test retry functionality
//            if (Math.random() < 0.3) {
//                throw new RuntimeException("Simulated failure for retry");
//            }

            // Reset retry count on success
            dataMap.put(RETRY_COUNT_KEY, 0);

        } catch (Exception e) {
            logger.error("Job execution failed. Retry count: {}", retryCount, e);

            retryCount++;
            dataMap.put(RETRY_COUNT_KEY, retryCount);

            if (retryCount <= MAX_RETRY_ATTEMPTS) {
                logger.info("Scheduling retry attempt {} of {}", retryCount, MAX_RETRY_ATTEMPTS);

                // Create a JobExecutionException with retry flag
                JobExecutionException jobException = new JobExecutionException("Job failed, scheduling retry", e,
                        false);
                jobException.setRefireImmediately(true);
                throw jobException;

            } else {
                logger.error("Job failed after {} attempts. Marking as failed without further retries.",
                        MAX_RETRY_ATTEMPTS);
                dataMap.put(RETRY_COUNT_KEY, 0); // Reset for future executions

                // Don't retry anymore
                throw new JobExecutionException("Job failed after maximum retry attempts", e, false);
            }
        }
    }

}