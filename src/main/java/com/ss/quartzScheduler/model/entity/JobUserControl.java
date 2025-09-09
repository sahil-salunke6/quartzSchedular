package com.ss.quartzScheduler.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing user control information for jobs stored in the database.
 */
@Entity
@Table(name = "DREAM_JOB_USER_CONTROL")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobUserControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "JOB_GROUP")
    private String jobGroup;

    @Column(name = "last_scheduled_time")
    private LocalDateTime lastScheduledTime;

    @Column(name = "actual_fire_time")
    private LocalDateTime actualFireTime;

    @Column(name = "next_fire_time")
    private LocalDateTime nextFireTime;

    @Column(name = "JOB_STATUS")
    private String status;

}

