package com.ss.quartzScheduler.service;

import com.ss.quartzScheduler.model.entity.JobExecutionMetadata;
import com.ss.quartzScheduler.model.entity.JobUserControl;
import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.repository.JobUserControlRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service to save job execution metadata and user control data to the database.
 * Implements Singleton pattern to ensure a single instance.
 */
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
    public static synchronized void init(JobExecutionMetadataRepository repository, JobUserControlRepository userControlRepository) {
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

    /**
     * Save job execution metadata to the database.
     *
     * @param meta JobExecutionMetadata object to be saved
     */
    public void saveMetadata(JobExecutionMetadata meta) {
        repository.save(meta);
    }

    /**
     * Save job user control data to the database.
     *
     * @param userControl JobUserControl object to be saved
     */
    public void saveUserdata(JobUserControl userControl) {
        userControlRepository.save(userControl);
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

}
