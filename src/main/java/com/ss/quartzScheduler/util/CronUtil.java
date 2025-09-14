package com.ss.quartzScheduler.util;

import com.ss.quartzScheduler.model.enums.DayOfWeekEnum;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for CRON expression operations
 */
public class CronUtil {

    private static final Logger logger = LoggerFactory.getLogger(CronUtil.class);

    // Default job name
    public static final String JOB_NAME = "QuartzJob";
    public static final String SCHEDULED_JOB_NAME = "QuartzScheduledJob";

    // Default group name for jobs
    public static final String GROUP_NAME = "QuartzGroup";

    /**
     * Validates a CRON expression
     *
     * @param cronExpression the CRON expression to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateCron(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            logger.error("Invalid CRON expression: {}", cronExpression, e);
            return false;
        }
    }

    /**
     * Gets the next execution time for a CRON expression
     *
     * @param cronExpression the CRON expression
     * @return the next execution time as LocalDateTime, or null if invalid
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date nextDate = cron.getNextValidTimeAfter(new Date());

            if (nextDate != null) {
                return LocalDateTime.ofInstant(nextDate.toInstant(), ZoneId.systemDefault());
            }

            return null;

        } catch (ParseException e) {
            logger.error("Failed to get next execution time for CRON: {}", cronExpression, e);
            return null;
        }
    }

    /**
     * Gets the next N execution times for a CRON expression
     *
     * @param cronExpression the CRON expression
     * @param count          number of execution times to retrieve
     * @return array of LocalDateTime representing next execution times
     */
    public static LocalDateTime[] getNextExecutionTimes(String cronExpression, int count) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            LocalDateTime[] executionTimes = new LocalDateTime[count];

            Date currentDate = new Date();
            for (int i = 0; i < count; i++) {
                Date nextDate = cron.getNextValidTimeAfter(currentDate);
                if (nextDate != null) {
                    executionTimes[i] = LocalDateTime.ofInstant(nextDate.toInstant(), ZoneId.systemDefault());
                    currentDate = nextDate;
                } else {
                    break;
                }
            }

            return executionTimes;

        } catch (ParseException e) {
            logger.error("Failed to get next execution times for CRON: {}", cronExpression, e);
            return new LocalDateTime[0];
        }
    }


    /**
     * Build a Quartz cron expression from parameters.
     *
     * @param second     - 0–59 (can be a fixed second, step value, or multiple seconds for custom)
     * @param minute     - 0–59
     * @param hour       - 0–23
     * @param day        - 1–31 (or ? if not applicable)
     * @param month      - 1–12 (or * for every month)
     * @param year       - full year (e.g., 2025), or * for any year
     * @param repeat     - true = recurring, false = one-time
     * @param interval   - repeat interval type: "secondly", "minutely", "hourly", "daily", "weekly", "monthly",
     *                   "yearly", "custom-seconds"
     * @param daysOfWeek - list of days of week (1=Sunday … 7=Saturday) for weekly jobs
     * @return Quartz cron expression as String
     */
    public static String generateCron(
            int second, int minute, int hour,
            int day, int month, int year,
            boolean repeat, String interval, List<Integer> daysOfWeek) {

        String format = String.format("%d %d %d %d %d ? %d", second, minute, hour, day, month, year);

        if (!repeat) {
            // one-time execution
            return format;
        }

        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }

        switch (interval.toLowerCase()) {
            case "secondly":
                // Runs every N seconds
                return "0/" + (second == 0 ? 1 : second) + " * * * * ?";

            case "custom-seconds":
                // Run at specific seconds (e.g., 5,15,30 of every minute)
                if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                    throw new IllegalArgumentException("Days of week not allowed for custom-seconds interval");
                }
                return second + " * * * * ?";

            case "minutely":
                return "0 0/" + (minute == 0 ? 1 : minute) + " * * * ?";

            case "hourly":
                return "0 " + minute + " 0/" + (hour == 0 ? 1 : hour) + " * * ?";

            case "daily":
                return String.format("%d %d %d * * ?", second, minute, hour);

            case "weekly":
                if (daysOfWeek == null || daysOfWeek.isEmpty()) {
                    throw new IllegalArgumentException("At least one day of week required for weekly schedule");
                }
                String dayOfWeekExpr = daysOfWeek.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                return String.format("%d %d %d ? * %s", second, minute, hour, dayOfWeekExpr);

            case "monthly":
                return String.format("%d %d %d %d * ?", second, minute, hour, day);

            case "yearly":
                return format;

            default:
                throw new IllegalArgumentException(
                        "Invalid interval. Choose: secondly, custom-seconds, minutely, hourly, daily, weekly, " +
                                "monthly, yearly");
        }
    }


    /**
     * Decode a CRON expression into a human-readable format.
     *
     * @param cron the CRON expression to decode
     * @return human-readable description of the CRON schedule
     */
    public static String decodeCron(String cron) {
        try {
            String[] parts = cron.split(" ");
            if (parts.length < 6 || parts.length > 7) {
                return "Invalid cron format";
            }

            String sec = parts[0];
            String min = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];
            String year = parts.length == 7 ? parts[6] : "*";

            String time = hour + ":" + min + ":" + sec;

            String days = dayOfWeek.equals("?") ? dayOfMonth : decodeDayOfWeek(dayOfWeek);
            String monthStr = month.equals("*") ? "every month" : month;
            String yearStr = year.equals("*") ? "every year" : year;

            return String.format("%s on %s of %s in %s", time, days, monthStr, yearStr);

        } catch (Exception e) {
            return "Error decoding cron: " + e.getMessage();
        }
    }

    /**
     * Decode day of week part of CRON expression
     *
     * @param dow day of week string from CRON
     * @return human-readable day(s) of week
     */
    private static String decodeDayOfWeek(String dow) {
        if (dow.contains(",")) {
            StringBuilder sb = new StringBuilder();
            for (String d : dow.split(",")) {
                int index = Integer.parseInt(d);
                sb.append(DayOfWeekEnum.values()[index - 1]).append(",");
            }
            return sb.substring(0, sb.length() - 1);
        } else if (dow.equals("*")) {
            return "every day";
        } else {
            int index = Integer.parseInt(dow) - 1;
            return DayOfWeekEnum.values()[index].name();
        }
    }


    /**
     * Utility methods for date-time conversions and formatting
     *
     * @param date Date to convert
     * @return LocalDateTime representation of the date
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Format LocalDateTime to String
     *
     * @param dateTime LocalDateTime to format
     * @return formatted date string
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Common CRON expressions
     */
    public static class CommonCronExpressions {
        public static final String EVERY_MINUTE = "0 * * * * ?";
        public static final String EVERY_5_MINUTES = "0 0/5 * * * ?";
        public static final String EVERY_15_MINUTES = "0 0/15 * * * ?";
        public static final String EVERY_30_MINUTES = "0 0/30 * * * ?";
        public static final String EVERY_HOUR = "0 0 * * * ?";
        public static final String DAILY_AT_MIDNIGHT = "0 0 0 * * ?";
        public static final String DAILY_AT_NOON = "0 0 12 * * ?";
        public static final String WEEKLY_MONDAY_9AM = "0 0 9 ? * MON";
        public static final String MONTHLY_FIRST_DAY_9AM = "0 0 9 1 * ?";
    }

}