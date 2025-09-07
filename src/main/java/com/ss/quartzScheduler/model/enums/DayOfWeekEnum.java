package com.ss.quartzScheduler.model.enums;

import lombok.Getter;

/**
 * Enum representing days of the week with corresponding Quartz values
 */
@Getter
public enum DayOfWeekEnum {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

    private final int quartzValue;

    DayOfWeekEnum(int quartzValue) {
        this.quartzValue = quartzValue;
    }

}
