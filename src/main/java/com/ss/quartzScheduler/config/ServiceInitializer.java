package com.ss.quartzScheduler.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.service.JobMetadataService;

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
        JobMetadataService.init(repository);
    }
}
