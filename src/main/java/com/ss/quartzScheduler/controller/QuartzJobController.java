package com.ss.quartzScheduler.controller;

import com.ss.quartzScheduler.exception.JobManagementException;
import com.ss.quartzScheduler.model.enums.DayOfWeekEnum;
import com.ss.quartzScheduler.model.enums.IntervalType;
import com.ss.quartzScheduler.model.SuspensionInfo;
import com.ss.quartzScheduler.model.dto.*;
import com.ss.quartzScheduler.service.QuartzJobManagementService;
import com.ss.quartzScheduler.util.CronUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ss.quartzScheduler.util.CronUtil.JOB_NAME;

@Slf4j
@RestController
@RequestMapping("/quartz/job")
@RequiredArgsConstructor
@Tag(name = "Quartz Job Management", description = "APIs for managing Quartz scheduled jobs")
public class QuartzJobController {

    private final QuartzJobManagementService jobManagementService;

    @PostMapping("/scheduleJob")
    @Operation(
            summary = "Schedule a job using cron expression",
            description = """
                    Schedule a job with flexible intervals.
                                    
                    **Intervals supported:**
                    - `secondly` → runs every N seconds (uses `second` as frequency).
                    - `custom-seconds` → runs at specific seconds of every minute.
                    - `minutely` → runs every N minutes (uses `minute` as frequency).
                    - `hourly` → runs every N hours (uses `hour` as frequency).
                    - `daily` → runs once a day at given time.
                    - `weekly` → runs on given days of week at specified time.
                    - `monthly` → runs on given day of each month.
                    - `yearly` → runs once per year at the specified date/time.
                                    
                    **Notes:**
                    - `daysOfWeek` is **required** for `weekly` interval.
                    - For one-time jobs, set `repeat=false`.
                    - Year is only used for one-time or yearly jobs.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job scheduled successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid interval, missing fields, or invalid cron expression",
                    content = @Content(schema = @Schema(example = "{ \"success\": false, \"error\": \"At least one " +
                            "day of week required for weekly schedule\" }"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(example = "{ \"success\": false, \"error\": \"Unexpected " +
                            "server error\" }"))
            )
    })
    public ResponseEntity<Map<String, Object>> scheduleJob(
            @Parameter(description = "Second (0-59). For 'secondly' interval, defines frequency.")
            @RequestParam(defaultValue = "0") int second,

            @Parameter(description = "Minute (0-59). For 'minutely' interval, defines frequency.")
            @RequestParam(defaultValue = "0") int minute,

            @Parameter(description = "Hour (0-23). For 'hourly' interval, defines frequency.")
            @RequestParam(defaultValue = "0") int hour,

            @Parameter(description = "Day of month (1-31)")
            @RequestParam(defaultValue = "1") int day,

            @Parameter(description = "Month (1-12)")
            @RequestParam(defaultValue = "1") int month,

            @Parameter(description = "Year (e.g., 2025). Used only for one-time or yearly jobs.")
            @RequestParam(defaultValue = "2025") int year,

            @Parameter(description = "Repeat job? true=recurring, false=one-time. Null=one-time by default.")
            @RequestParam(required = false) Boolean repeat,

            @Parameter(description = "Interval type (secondly, custom-seconds, minutely, hourly, daily, weekly, " +
                    "monthly, yearly).")
            @RequestParam IntervalType interval,

            @Parameter(description = "Days of week (e.g., MONDAY, TUESDAY). Required if interval=weekly.")
            @RequestParam(required = false) List<DayOfWeekEnum> daysOfWeek,

            @Parameter(description = "Job name")
            @RequestParam(defaultValue = JOB_NAME) String jobName) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Integer> dayValues = (daysOfWeek != null)
                    ? daysOfWeek.stream().map(DayOfWeekEnum::getQuartzValue).collect(Collectors.toList())
                    : Collections.emptyList();

            // Generate CRON expression from parameters
            String cronExpression = CronUtil.generateCron(
                    second, minute, hour, day, month, year, repeat, interval.name(), dayValues);

            // Validate CRON expression
            if (!CronUtil.validateCron(cronExpression)) {
                response.put("success", false);
                response.put("error", "Invalid CRON expression");
                return ResponseEntity.badRequest().body(response);
            }

            jobManagementService.scheduleJob(jobName, cronExpression);

            response.put("success", true);
            response.put("message", "Job scheduled successfully");
            response.put("jobName", jobName);
            response.put("cronExpression", cronExpression);
            response.put("nextExecutionTime", CronUtil.getNextExecutionTime(cronExpression));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to schedule job", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/triggerInstantly")
    @Operation(summary = "Trigger a job immediately",
            description = "Triggers a specific job to run immediately")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job triggered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Job not found or suspended"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<ApiResponse<Void>> triggerInstantJob(
            @Valid @RequestBody JobTriggerRequest request) throws JobManagementException {

        try {
            jobManagementService.triggerJob(request.getJobName(), request.getGroupName());

            String message = String.format("Job %s.%s triggered successfully",
                    request.getJobName(), request.getGroupName());
            return ResponseEntity.ok(ApiResponse.success(message));

        } catch (SchedulerException e) {
            throw new JobManagementException("Failed to trigger job: " + e.getMessage(), e);
        }
    }

    @PostMapping("/suspend/temporary")
    @Operation(summary = "Suspend job temporarily",
            description = "Suspends a job until a specified date and time")
    public ResponseEntity<ApiResponse<SuspensionInfoDto>> suspendJobTemporary(
            @Valid @RequestBody SuspensionRequest request) throws JobManagementException {

        if (request.getResumeDateTime() == null) {
            throw new JobManagementException("Resume date time is required for temporary suspension");
        }

        if (request.getResumeDateTime().isBefore(LocalDateTime.now())) {
            throw new JobManagementException("Resume date time must be in the future");
        }

        try {
            jobManagementService.suspendJobTemporary(
                    request.getJobName(),
                    request.getGroupName(),
                    request.getResumeDateTime()
            );

            SuspensionInfoDto suspensionInfo = createSuspensionInfoDto(
                    request.getJobName(),
                    request.getGroupName()
            );

            String message = String.format("Job %s.%s suspended temporarily until %s",
                    request.getJobName(), request.getGroupName(),
                    request.getResumeDateTime());
            return ResponseEntity.ok(ApiResponse.success(message, suspensionInfo));

        } catch (SchedulerException e) {
            throw new JobManagementException("Failed to suspend job temporarily: " + e.getMessage(), e);
        }
    }

    @PostMapping("/suspend/permanent")
    @Operation(summary = "Suspend job permanently",
            description = "Suspends a job permanently until manually resumed")
    public ResponseEntity<ApiResponse<SuspensionInfoDto>> suspendJobPermanently(
            @Valid @RequestBody SuspensionRequest request) throws JobManagementException {

        try {
            jobManagementService.suspendJobPermanently(
                    request.getJobName(),
                    request.getGroupName(),
                    request.getReason()
            );

            SuspensionInfoDto suspensionInfo = createSuspensionInfoDto(
                    request.getJobName(),
                    request.getGroupName()
            );

            String message = String.format("Job %s.%s suspended permanently",
                    request.getJobName(), request.getGroupName());
            return ResponseEntity.ok(ApiResponse.success(message, suspensionInfo));

        } catch (SchedulerException e) {
            throw new JobManagementException("Failed to suspend job permanently: " + e.getMessage(), e);
        }
    }

    @PostMapping("/suspend/revoke")
    @Operation(summary = "Revoke job suspension",
            description = "Removes suspension and resumes the job")
    public ResponseEntity<ApiResponse<Void>> revokeSuspension(
            @Parameter(description = "Job name") @RequestParam String jobName,
            @Parameter(description = "Group name") @RequestParam String groupName)
            throws JobManagementException {

        try {
            jobManagementService.revokeSuspension(jobName, groupName);

            String message = String.format("Suspension revoked for job %s.%s", jobName, groupName);
            return ResponseEntity.ok(ApiResponse.success(message));

        } catch (SchedulerException e) {
            throw new JobManagementException("Failed to revoke suspension: " + e.getMessage(), e);
        }
    }

    @PostMapping("/suspend/extend")
    @Operation(summary = "Extend job suspension",
            description = "Extends the suspension period for a temporarily suspended job")
    public ResponseEntity<ApiResponse<SuspensionInfoDto>> extendSuspension(
            @Valid @RequestBody SuspensionRequest request) throws JobManagementException {

        if (request.getResumeDateTime() == null) {
            throw new JobManagementException("New resume date time is required");
        }

        if (request.getResumeDateTime().isBefore(LocalDateTime.now())) {
            throw new JobManagementException("New resume date time must be in the future");
        }

        try {
            jobManagementService.extendSuspension(
                    request.getJobName(),
                    request.getGroupName(),
                    request.getResumeDateTime()
            );

            SuspensionInfoDto suspensionInfo = createSuspensionInfoDto(
                    request.getJobName(),
                    request.getGroupName()
            );

            String message = String.format("Suspension extended for job %s.%s until %s",
                    request.getJobName(), request.getGroupName(),
                    request.getResumeDateTime());
            return ResponseEntity.ok(ApiResponse.success(message, suspensionInfo));

        } catch (SchedulerException e) {
            throw new JobManagementException("Failed to extend suspension: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{jobName}/{groupName}/status")
    @Operation(summary = "Get job status",
            description = "Gets the current status and suspension info of a job")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJobStatus(
            @Parameter(description = "Job name") @PathVariable String jobName,
            @Parameter(description = "Group name") @PathVariable String groupName) {

        Map<String, Object> status = new HashMap<>();
        status.put("jobName", jobName);
        status.put("groupName", groupName);
        status.put("suspended", jobManagementService.isJobSuspended(jobName, groupName));

        SuspensionInfo suspensionInfo = jobManagementService.getSuspensionInfo(jobName, groupName);
        if (suspensionInfo != null) {
            status.put("suspensionInfo", convertToDto(suspensionInfo, jobName, groupName));
        }

        return ResponseEntity.ok(ApiResponse.success("Job status retrieved", status));
    }

    @GetMapping("/suspended")
    @Operation(summary = "Get all suspended jobs",
            description = "Returns a list of all currently suspended jobs")
    public ResponseEntity<ApiResponse<Map<String, SuspensionInfoDto>>> getAllSuspendedJobs() {

        Map<String, SuspensionInfo> suspendedJobs = jobManagementService.getAllSuspendedJobs();
        Map<String, SuspensionInfoDto> result = suspendedJobs.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            String[] parts = entry.getKey().split("\\.");
                            return convertToDto(entry.getValue(), parts[0], parts[1]);
                        }
                ));

        String message = String.format("Found %d suspended jobs", result.size());
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    private SuspensionInfoDto createSuspensionInfoDto(String jobName, String groupName) {
        SuspensionInfo info = jobManagementService.getSuspensionInfo(jobName, groupName);
        return info != null ? convertToDto(info, jobName, groupName) : null;
    }

    private SuspensionInfoDto convertToDto(SuspensionInfo info, String jobName, String groupName) {
        return new SuspensionInfoDto(
                info.getType(),
                info.getSuspendedAt(),
                info.getResumeDateTime(),
                info.getReason(),
                jobName,
                groupName
        );
    }
}