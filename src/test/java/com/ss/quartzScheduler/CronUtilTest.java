package com.ss.quartzScheduler;

import com.ss.quartzScheduler.util.CronUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CronUtil
 */
class CronUtilTest {

    @Test
    void testValidCronExpressions() {
        assertTrue(CronUtil.validateCron("0 0/5 * * * ?"));
        assertTrue(CronUtil.validateCron("0 0 12 * * ?"));
        assertTrue(CronUtil.validateCron("0 15 10 ? * MON-FRI"));
    }

    @Test
    void testInvalidCronExpressions() {
        assertFalse(CronUtil.validateCron("invalid"));
        assertFalse(CronUtil.validateCron("* * * * * * *"));
        assertFalse(CronUtil.validateCron(""));
    }

    @Test
    void testGetNextExecutionTime() {
        LocalDateTime nextTime = CronUtil.getNextExecutionTime("0 0/5 * * * ?");
        assertNotNull(nextTime);
        assertTrue(nextTime.isAfter(LocalDateTime.now()));
    }

    @Test
    void testGetNextExecutionTimes() {
        LocalDateTime[] times = CronUtil.getNextExecutionTimes("0 0/5 * * * ?", 3);
        assertEquals(3, times.length);

        for (int i = 1; i < times.length; i++) {
            assertTrue(times[i].isAfter(times[i - 1]));
        }
    }

    @Test
    void testOneTimeExecution() {
        String cron = CronUtil.generateCron(0, 30, 10, 5, 9, 2025,
                false, "daily", null);

        assertEquals("0 30 10 5 9 ? 2025", cron);
    }

    @Test
    void testSecondlyRecurring() {
        String cron = CronUtil.generateCron(5, 0, 0, 0, 0, 0,
                true, "secondly", null);

        assertEquals("0/5 * * * * ?", cron);
    }

    @Test
    void testMinutelyRecurring() {
        String cron = CronUtil.generateCron(0, 10, 0, 0, 0, 0,
                true, "minutely", null);

        assertEquals("0 0/10 * * * ?", cron);
    }

    @Test
    void testHourlyRecurring() {
        String cron = CronUtil.generateCron(0, 15, 2, 0, 0, 0,
                true, "hourly", null);

        assertEquals("0 15 0/2 * * ?", cron);
    }

    @Test
    void testDailyRecurring() {
        String cron = CronUtil.generateCron(0, 45, 8, 0, 0, 0,
                true, "daily", null);

        assertEquals("0 45 8 * * ?", cron);
    }

    @Test
    void testWeeklyRecurring() {
        String cron = CronUtil.generateCron(0, 0, 9, 0, 0, 0,
                true, "weekly", Arrays.asList(2, 4)); // Monday & Wednesday

        assertEquals("0 0 9 ? * 2,4", cron);
    }

    @Test
    void testWeeklyRecurringWithoutDaysThrowsError() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                CronUtil.generateCron(0, 0, 9, 0, 0, 0,
                        true, "weekly", Collections.emptyList()));

        assertEquals("At least one day of week required for weekly schedule", ex.getMessage());
    }

    @Test
    void testMonthlyRecurring() {
        String cron = CronUtil.generateCron(0, 30, 7, 15, 0, 0,
                true, "monthly", null);

        assertEquals("0 30 7 15 * ?", cron);
    }

    @Test
    void testYearlyRecurring() {
        String cron = CronUtil.generateCron(0, 0, 12, 25, 12, 2025,
                true, "yearly", null);

        assertEquals("0 0 12 25 12 ? 2025", cron);
    }

    @Test
    void testInvalidIntervalThrowsError() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                CronUtil.generateCron(0, 0, 0, 0, 0, 0,
                        true, "invalid", null));

        assertTrue(ex.getMessage().contains("Invalid interval"));
    }

    @Test
    void testCommonCronExpressions() {
        assertTrue(CronUtil.validateCron(CronUtil.CommonCronExpressions.EVERY_5_MINUTES));
        assertTrue(CronUtil.validateCron(CronUtil.CommonCronExpressions.DAILY_AT_MIDNIGHT));
        assertTrue(CronUtil.validateCron(CronUtil.CommonCronExpressions.WEEKLY_MONDAY_9AM));
    }
}