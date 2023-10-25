package ru.itq.timeinstatus.utils;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static ru.itq.timeinstatus.utils.Constants.*;

public class WorkingDayUtils {
    public static Long getTimeSpent(LocalDateTime last, LocalDateTime next) {
        if (last.toLocalDate().equals(next.toLocalDate())) {
            return getTimeSpentToDate(last, next);
        } else {
            long counter = getTimeSpentToDate(last, next.withDayOfYear(last.getDayOfYear()).toLocalDate().atTime(23, 59, 59, 59));
            long l = Duration.between(last, next).toDays();
            for (int i = 1; i < l; i++) {
                counter += getTimeSpentToDate(
                        last.toLocalDate().plusDays(i).atTime(0, 0, 0, 0),
                        next.toLocalDate().plusDays(i).atTime(23, 59, 59, 59)
                );
            }
            return counter + getTimeSpentToDate(last.withDayOfYear(next.getDayOfYear()).toLocalDate().atTime(0, 0, 0, 0), next);
        }

    }

    private static long getTimeSpentToDate(LocalDateTime last, LocalDateTime next) {
        LocalDateTime lastWorkingDayStart = next.toLocalDate().atTime(WORKING_DAY_START_HOUR, WORKING_DAY_START_MINUTE, 0, 0);
        if (last.isBefore(lastWorkingDayStart)) {
            last = lastWorkingDayStart;
        }
        LocalDateTime nextWorkingDayEnd = next.toLocalDate().atTime(WORKING_DAY_END_HOUR, WORKING_DAY_END_MINUTE, 0, 0);
        if (next.isAfter(nextWorkingDayEnd)) {
            next = nextWorkingDayEnd;
        }
        return Date.from(next.atZone(ZoneId.systemDefault()).toInstant()).getTime() - Date.from(last.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }
}
