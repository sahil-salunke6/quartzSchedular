package com.ss.quartzScheduler.repository;

import com.ss.quartzScheduler.model.JobExecutionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for JobExecutionMetadata entity.
 */
public interface JobExecutionMetadataRepository extends JpaRepository<JobExecutionMetadata, Long> {
}
