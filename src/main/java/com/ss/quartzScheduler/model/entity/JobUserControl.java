package com.ss.quartzScheduler.model.entity;

import com.ss.quartzScheduler.model.SuspensionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "QRTZ_J0OB_USER_CONTROL")
@Builder
@Data
public class JobUserControl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "JOB_NAME")
    private String suspendedJobName;

    @Column(name = "JOB_GROUP")
    private String suspendedJobGroup;

    @Column(name = "SUSPENSION_TYPE")
    private final SuspensionType suspendedType;

    @Column(name = "SUSPENDED_AT")
    private final LocalDateTime suspendedAt;

    @Column(name = "JOB_RESUME_AT")
    private LocalDateTime resumeAt;

    @Column(name = "SUSPENSION_REASON")
    private final String suspensionReason;

    @Column(name = "JOB_STATUS")
    private final String status;

}

