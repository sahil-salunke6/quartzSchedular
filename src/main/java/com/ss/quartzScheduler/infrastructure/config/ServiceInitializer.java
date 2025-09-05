package com.ss.quartzScheduler.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.ss.quartzScheduler.domain.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.usecase.SaveJobMetaDataUsaCase;

/**
 * Service initializer to set up JobMetadataService with the repository
 * after the Spring context is initialized.
 */
@Configuration
public class ServiceInitializer {

    private final JobExecutionMetadataRepository repository;

    public ServiceInitializer(JobExecutionMetadataRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        SaveJobMetaDataUsaCase.init(repository);
    }
}
