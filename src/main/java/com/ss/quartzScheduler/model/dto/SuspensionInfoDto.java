package com.ss.quartzScheduler.model.dto;

import com.ss.quartzScheduler.model.SuspensionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Job suspension information")
public class SuspensionInfoDto {

    @Schema(description = "Type of suspension", example = "TEMPORARY")
    private SuspensionType type;

    @Schema(description = "When the job was suspended")
    private LocalDateTime suspendedAt;

    @Schema(description = "When the job will be resumed (for temporary suspensions)")
    private LocalDateTime resumeDateTime;

    @Schema(description = "Reason for suspension")
    private String reason;

    @Schema(description = "Job name")
    private String jobName;

    @Schema(description = "Job group")
    private String groupName;
}