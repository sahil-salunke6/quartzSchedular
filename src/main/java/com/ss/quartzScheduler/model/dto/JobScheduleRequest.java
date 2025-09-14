package com.ss.quartzScheduler.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.ss.quartzScheduler.util.CronUtil.CommonCronExpressions.JOB_NAME;

@Data
@Schema(description = "Request to Schedule a job with cron expression")
public class JobScheduleRequest {

    @NotBlank(message = "Job name is required")
    @Schema(description = "Name of the job", example = JOB_NAME)
    private String jobName;

    private int second;
    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private boolean repeat;
    private String interval;
    private List<Integer> daysOfWeek;
}