package com.ss.quartzScheduler;

import com.ss.quartzScheduler.model.enums.DayOfWeekEnum;
import com.ss.quartzScheduler.util.CronUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CronUtil class
 */
class CronUtilTest {

    @Test
    void testValidateCron_valid() {
        assertTrue(CronUtil.validateCron(CronUtil.CommonCronExpressions.EVERY_MINUTE));
    }

    @Test
    void testValidateCron_invalid() {
        assertFalse(CronUtil.validateCron("invalid-cron"));
    }

    @Test
    void testGetNextExecutionTime_valid() {
        LocalDateTime next = CronUtil.getNextExecutionTime(CronUtil.CommonCronExpressions.EVERY_MINUTE);
        assertNotNull(next);
        assertTrue(next.isAfter(LocalDateTime.now()));
    }

    @Test
    void testGetNextExecutionTime_invalid() {
        assertNull(CronUtil.getNextExecutionTime("invalid-cron"));
    }

    @Test
    void testGetNextExecutionTimes_valid() {
        LocalDateTime[] times = CronUtil.getNextExecutionTimes(CronUtil.CommonCronExpressions.EVERY_MINUTE, 3);
        assertEquals(3, times.length);
        for (LocalDateTime t : times) {
            assertNotNull(t);
        }
    }

    @Test
    void testGetNextExecutionTimes_invalid() {
        LocalDateTime[] times = CronUtil.getNextExecutionTimes("invalid-cron", 3);
        assertEquals(0, times.length);
    }

    @Test
    void testGenerateCron_oneTime() {
        String cron = CronUtil.generateCron(0, 0, 10, 1, 1, 2025, false, null, null);
        assertEquals("0 0 10 1 1 ? 2025", cron);
    }

    @Test
    void testGenerateCron_secondly() {
        String cron = CronUtil.generateCron(5, 0, 0, 0, 0, 0, true, "secondly", null);
        assertEquals("0/5 * * * * ?", cron);
    }

    @Test
    void testGenerateCron_customSeconds() {
        String cron = CronUtil.generateCron(5, 0, 0, 0, 0, 0, true, "custom-seconds", null);
        assertEquals("5 * * * * ?", cron);
    }

    @Test
    void testGenerateCron_minutely() {
        String cron = CronUtil.generateCron(0, 5, 0, 0, 0, 0, true, "minutely", null);
        assertEquals("0 0/5 * * * ?", cron);
    }

    @Test
    void testGenerateCron_hourly() {
        String cron = CronUtil.generateCron(0, 5, 2, 0, 0, 0, true, "hourly", null);
        assertEquals("0 5 0/2 * * ?", cron);
    }

    @Test
    void testGenerateCron_daily() {
        String cron = CronUtil.generateCron(0, 0, 12, 0, 0, 0, true, "daily", null);
        assertEquals("0 0 12 * * ?", cron);
    }

    @Test
    void testGenerateCron_weekly() {
        List<Integer> days = Arrays.asList(1, 3, 5);
        String cron = CronUtil.generateCron(0, 0, 12, 0, 0, 0, true, "weekly", days);
        assertEquals("0 0 12 ? * 1,3,5", cron);
    }

    @Test
    void testGenerateCron_weekly_noDays() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> CronUtil.generateCron(0, 0, 12, 0, 0, 0, true, "weekly", null));
        assertTrue(e.getMessage().contains("At least one day of week required"));
    }

    @Test
    void testGenerateCron_monthly() {
        String cron = CronUtil.generateCron(0, 0, 12, 1, 0, 0, true, "monthly", null);
        assertEquals("0 0 12 1 * ?", cron);
    }

    @Test
    void testGenerateCron_yearly() {
        String cron = CronUtil.generateCron(0, 0, 12, 1, 1, 2025, true, "yearly", null);
        assertEquals("0 0 12 1 1 ? 2025", cron);
    }

    @Test
    void testGenerateCron_invalidInterval() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> CronUtil.generateCron(0, 0, 0, 0, 0, 0, true, "invalid", null));
        assertTrue(e.getMessage().contains("Invalid interval"));
    }

    @Test
    void testGenerateCron_nullInterval() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> CronUtil.generateCron(0, 0, 0, 0, 0, 0, true, null, null));
        assertTrue(e.getMessage().contains("Interval cannot be null"));
    }

    @Test
    void testDecodeCron_valid() {
        String decoded = CronUtil.decodeCron("0 0 12 * * ?");
        assertNotNull(decoded);
        assertTrue(decoded.contains("12:0:0"));
    }

    @Test
    void testDecodeCron_invalid() {
        String decoded = CronUtil.decodeCron("invalid cron");
        assertTrue(decoded.contains("Invalid cron format"));
    }

    @Test
    void testDecodeDayOfWeek_single() throws Exception {
        String dow = "1";
        // Using reflection to test private method
        var method = CronUtil.class.getDeclaredMethod("decodeDayOfWeek", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, dow);
        assertEquals(DayOfWeekEnum.SUNDAY.name(), result);
    }

    @Test
    void testDecodeDayOfWeek_multiple() throws Exception {
        String dow = "1,2";
        var method = CronUtil.class.getDeclaredMethod("decodeDayOfWeek", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, dow);
        assertTrue(result.contains(DayOfWeekEnum.SUNDAY.name()));
        assertTrue(result.contains(DayOfWeekEnum.MONDAY.name()));
    }

    @Test
    void testConvertToLocalDateTime_null() {
        assertNull(CronUtil.convertToLocalDateTime(null));
    }

    @Test
    void testConvertToLocalDateTime_nonNull() {
        Date now = new Date();
        LocalDateTime ldt = CronUtil.convertToLocalDateTime(now);
        assertNotNull(ldt);
        assertEquals(now.getYear(), ldt.getYear() - 1900); // Date year offset
    }

    @Test
    void testFormatDate() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = CronUtil.formatDate(now);
        assertNotNull(formatted);
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
