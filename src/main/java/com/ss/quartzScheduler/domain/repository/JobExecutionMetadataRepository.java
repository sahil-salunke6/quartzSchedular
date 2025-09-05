package com.ss.quartzScheduler.domain.repository;

import com.ss.quartzScheduler.domain.entity.JobExecutionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for JobExecutionMetadata entity.
 */
public interface JobExecutionMetadataRepository extends JpaRepository<JobExecutionMetadata, Long> {
}
