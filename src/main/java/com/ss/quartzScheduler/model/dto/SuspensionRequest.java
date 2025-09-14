package com.ss.quartzScheduler.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

import static com.ss.quartzScheduler.util.CronUtil.CommonCronExpressions.GROUP_NAME;
import static com.ss.quartzScheduler.util.CronUtil.CommonCronExpressions.JOB_NAME;

@Data
@Schema(description = "Request to suspend a job")
public class SuspensionRequest {

    @NotBlank(message = "Job name is required")
    @Schema(description = "Name of the job", example = JOB_NAME)
    private String jobName;

    @NotBlank(message = "Group name is required")
    @Schema(description = "Group name of the job", example = GROUP_NAME)
    private String groupName;

    @Schema(description = "Resume date time for temporary suspension", example = "2025-09-08T10:00:00")
    private LocalDateTime resumeDateTime;

    @Schema(description = "Reason for suspension", example = "System maintenance")
    private String reason;
}