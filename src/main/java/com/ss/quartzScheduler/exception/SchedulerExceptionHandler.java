package com.ss.quartzScheduler.exception;

import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Quartz Scheduler exceptions
 */
@ControllerAdvice
public class SchedulerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerExceptionHandler.class);

    /**
     * Handle SchedulerException and return a structured JSON response
     *
     * @param e the SchedulerException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<Map<String, Object>> handleSchedulerException(SchedulerException e) {
        logger.error("SchedulerException occurred", e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Scheduler error: " + e.getMessage());
        errorResponse.put("type", "SchedulerException");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle JobExecutionException and return a structured JSON response
     *
     * @param e the JobExecutionException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(JobExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleJobExecutionException(JobExecutionException e) {
        logger.error("JobExecutionException occurred", e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Job execution error: " + e.getMessage());
        errorResponse.put("type", "JobExecutionException");
        errorResponse.put("refireImmediately", e.refireImmediately());
        errorResponse.put("unscheduleAllTriggers", e.unscheduleAllTriggers());
        errorResponse.put("unscheduleFiringTrigger", e.unscheduleFiringTrigger());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle generic Exception and return a structured JSON response
     *
     * @param e the Exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Unexpected exception occurred", e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Unexpected error: " + e.getMessage());
        errorResponse.put("type", e.getClass().getSimpleName());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
