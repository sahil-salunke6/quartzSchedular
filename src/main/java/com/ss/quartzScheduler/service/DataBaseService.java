package com.ss.quartzScheduler.service;

import com.ss.quartzScheduler.model.entity.JobExecutionMetadata;
import com.ss.quartzScheduler.model.entity.JobUserControl;
import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.repository.JobUserControlRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ss.quartzScheduler.util.CronUtil.formatDate;

/**
 * Service to save job execution metadata and user control data to the database.
 * Implements Singleton pattern to ensure a single instance.
 */
@Slf4j
public class DataBaseService {

    // Singleton instance
    private static DataBaseService instance;

    // Repository for job execution metadata
    private final JobExecutionMetadataRepository repository;

    // Repository for job user control
    private final JobUserControlRepository userControlRepository;

    // Private constructor to enforce singleton pattern
    private DataBaseService(JobExecutionMetadataRepository repository, JobUserControlRepository userControlRepository) {
        this.repository = repository;
        this.userControlRepository = userControlRepository;
    }

    // Synchronized method to initialize the singleton instance
    public static synchronized void init(JobExecutionMetadataRepository repository,
                                         JobUserControlRepository userControlRepository) {
        if (instance == null) {
            instance = new DataBaseService(repository, userControlRepository);
        }
    }

    // Method to get the singleton instance
    public static DataBaseService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JobMetadataService not initialized. Call init() first.");
        }
        return instance;
    }

    public Optional<JobUserControl> findByJobNameAndJobGroup(String jobName, String jobGroup) {
        return userControlRepository.findByJobNameAndJobGroup(jobName, jobGroup);
    }

    public Optional<JobUserControl> getStatus(String jobName, String jobGroup) {
        return userControlRepository.getStatus(jobName, jobGroup);
    }

    public List<JobUserControl> getAllSuspendedJobs() {
        return userControlRepository.getAllSuspendedJobs();
    }

    /**
     * Save job execution metadata to the database.
     *
     * @param context JobExecutionContext context
     */
    public void storeJobMetadata(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // Store scheduling time metadata in QRTZ_JOB_DETAILS as a JOB_DATA in a blob format
        dataMap.put("lastScheduledTime", context.getScheduledFireTime());
        dataMap.put("actualFireTime", context.getFireTime());
        dataMap.put("nextFireTime", context.getNextFireTime());
        dataMap.put("executionTime", LocalDateTime.now());

        // Store scheduling time metadata in DREAM_JOB_EXECUTION_METADATA table
        JobExecutionMetadata meta = JobExecutionMetadata.builder()
                .jobName(context.getJobDetail().getKey().getName())
                .jobGroup(context.getJobDetail().getKey().getGroup())
                .lastScheduledTime(formatDate(context.getScheduledFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .actualFireTime(formatDate(context.getFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .nextFireTime(context.getNextFireTime() != null ?
                        formatDate(context.getNextFireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()) : "N/A")
                .executionTime(formatDate(LocalDateTime.now()))
                .build();

        repository.save(meta);
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
    public void storeJobUserData(String jobName, String groupName, LocalDateTime lastScheduledTime,
                                 LocalDateTime actualFireTime, LocalDateTime nextFireTime, String status) {

        try {
            JobUserControl control =
                    findByJobNameAndJobGroup(jobName, groupName).orElse(JobUserControl.builder().build());

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

            userControlRepository.save(control);

            log.info("Stored job user data for {}.{} -> status={}", jobName, groupName, status);
        } catch (Exception e) {
            log.error("Failed to store job user data for {}.{}", jobName, groupName, e);
        }
    }

}
