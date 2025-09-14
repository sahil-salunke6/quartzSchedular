package com.ss.quartzScheduler.model.enums;

/**
 * Enum representing the status of a job
 */
public enum JobStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    FAILED,
    SUSPENDED_TEMP,
    SUSPENDED_PERM,
    RESUMED
}
