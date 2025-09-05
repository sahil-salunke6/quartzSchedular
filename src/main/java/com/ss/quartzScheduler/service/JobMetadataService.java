package com.ss.quartzScheduler.service;

import com.ss.quartzScheduler.model.JobExecutionMetadata;
import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;

/**
 * Service to handle job metadata operations.
 * Implements Singleton pattern to ensure a single instance.
 */
public class JobMetadataService {

    // Singleton instance
    private static JobMetadataService instance;

    // Repository for job execution metadata
    private final JobExecutionMetadataRepository repository;

    // Private constructor to enforce singleton pattern
    private JobMetadataService(JobExecutionMetadataRepository repository) {
        this.repository = repository;
    }

    // Synchronized method to initialize the singleton instance
    public static synchronized void init(JobExecutionMetadataRepository repository) {
        if (instance == null) {
            instance = new JobMetadataService(repository);
        }
    }

    // Method to get the singleton instance
    public static JobMetadataService getInstance() {
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
}
