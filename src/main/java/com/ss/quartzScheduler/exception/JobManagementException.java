package com.ss.quartzScheduler.exception;

/**
 * Custom exception for job management errors
 */
public class JobManagementException extends Exception {
    public JobManagementException(String message) {
        super(message);
    }

    public JobManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}