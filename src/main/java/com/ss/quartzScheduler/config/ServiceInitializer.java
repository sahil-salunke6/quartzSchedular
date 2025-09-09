package com.ss.quartzScheduler.config;

import com.ss.quartzScheduler.repository.JobExecutionMetadataRepository;
import com.ss.quartzScheduler.repository.JobUserControlRepository;
import com.ss.quartzScheduler.service.DataBaseService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * ServiceInitializer is responsible for initializing the database service
 * with the required repositories after the application context is set up.
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
