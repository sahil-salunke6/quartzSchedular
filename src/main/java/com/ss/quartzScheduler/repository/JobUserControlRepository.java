package com.ss.quartzScheduler.repository;

import com.ss.quartzScheduler.model.entity.JobUserControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobUserControl entity.
 */
public interface JobUserControlRepository extends JpaRepository<JobUserControl, Long> {
    Optional<JobUserControl> findByJobNameAndJobGroup(String jobName, String jobGroup);

    @Query("SELECT j FROM JobUserControl j WHERE j.jobName = :jobName AND j.jobGroup = :jobGroup")
    Optional<JobUserControl> getStatus(String jobName, String jobGroup);

    // Get all suspended jobs (assuming 'SUSPENDED' is the status)
    @Query("SELECT j FROM JobUserControl j WHERE j.status = 'SUSPENDED'")
    List<JobUserControl> getAllSuspendedJobs();
}
