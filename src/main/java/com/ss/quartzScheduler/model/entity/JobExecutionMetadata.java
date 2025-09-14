package com.ss.quartzScheduler.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing job execution metadata stored in the database.
 */
@Entity
@Table(name = "DREAM_JOB_EXECUTION_METADATA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "job_group")
    private String jobGroup;

    @Column(name = "last_scheduled_time")
    private String lastScheduledTime;

    @Column(name = "actual_fire_time")
    private String actualFireTime;

    @Column(name = "next_fire_time")
    private String nextFireTime;

    @Column(name = "execution_time")
    private String executionTime;
}
