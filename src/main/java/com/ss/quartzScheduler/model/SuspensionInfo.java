package com.ss.quartzScheduler.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SuspensionInfo {
    // Getters and setters
    private final SuspensionType type;
    private final LocalDateTime suspendedAt;
    private LocalDateTime resumeDateTime;
    private final String reason;

    public SuspensionInfo(SuspensionType type, LocalDateTime suspendedAt,
                          LocalDateTime resumeDateTime, String reason) {
        this.type = type;
        this.suspendedAt = suspendedAt;
        this.resumeDateTime = resumeDateTime;
        this.reason = reason;
    }

    public void setResumeDateTime(LocalDateTime resumeDateTime) {
        this.resumeDateTime = resumeDateTime;
    }

    @Override
    public String toString() {
        return String.format("SuspensionInfo{type=%s, suspendedAt=%s, resumeDateTime=%s, reason='%s'}",
                type, suspendedAt, resumeDateTime, reason);
    }
}