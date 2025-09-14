package com.ss.quartzScheduler.service;

import com.ss.quartzScheduler.model.entity.JobExecutionMetadata;
import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;

/**
 * Service to handle job metadata operations.
 * Implements Singleton pattern to ensure a single instance.
 */
public class SaveJobMetaDataService {

    // Singleton instance
    private static SaveJobMetaDataService instance;

    // Repository for job execution metadata
    private final JobExecutionMetadataRepository repository;

    // Private constructor to enforce singleton pattern
    private SaveJobMetaDataService(JobExecutionMetadataRepository repository) {
        this.repository = repository;
    }

    // Synchronized method to initialize the singleton instance
    public static synchronized void init(JobExecutionMetadataRepository repository) {
        if (instance == null) {
            instance = new SaveJobMetaDataService(repository);
        }
    }

    // Method to get the singleton instance
    public static SaveJobMetaDataService getInstance() {
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
