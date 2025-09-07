package com.ss.quartzScheduler.config;

import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.repository.JobUserControlRepository;
import com.ss.quartzScheduler.service.DataBaseService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Service initializer to set up JobMetadataService with the repository
 * after the Spring context is initialized.
 */
@Configuration
public class ServiceInitializer {

    private final JobExecutionMetadataRepository jobExecutionMetadataRepository;
    private final JobUserControlRepository userControlRepository;

    public ServiceInitializer(JobExecutionMetadataRepository jobExecutionMetadataRepository,
                              JobUserControlRepository userControlRepository) {
        this.jobExecutionMetadataRepository = jobExecutionMetadataRepository;
        this.userControlRepository = userControlRepository;
    }

    @PostConstruct
    public void init() {
        DataBaseService.init(jobExecutionMetadataRepository, userControlRepository);

    }
}
