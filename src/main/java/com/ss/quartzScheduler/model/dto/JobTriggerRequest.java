package com.ss.quartzScheduler.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.ss.quartzScheduler.util.CronUtil.GROUP_NAME;
import static com.ss.quartzScheduler.util.CronUtil.JOB_NAME;

/**
 * Request DTO for triggering a job
 */
@Data
@Schema(description = "Request to trigger a job")
public class JobTriggerRequest {

    @NotBlank(message = "Job name is required")
    @Schema(description = "Name of the job", example = JOB_NAME)
    private String jobName;

    @NotBlank(message = "Group name is required")
    @Schema(description = "Group name of the job", example = GROUP_NAME)
    private String groupName;

}