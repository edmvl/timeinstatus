package ru.itq.timeinstatus.utils;

import java.sql.Date;
import java.time.*;

import static ru.itq.timeinstatus.utils.Constants.*;

public class WorkingDayUtils {
    public static Long getTimeSpent(LocalDateTime last, LocalDateTime next) {
        if (last.toLocalDate().equals(next.toLocalDate())) {
            return getTimeSpentToDate(last, next);
        } else {
            long counter = getTimeSpentToDate(last, next.withDayOfYear(last.getDayOfYear()).toLocalDate().atTime(23, 59, 59, 59));
            long l = Duration.between(last, next).toDays();
            for (int i = 1; i < l; i++) {
                LocalDate ticker = last.toLocalDate().plusDays(i);
                counter += getTimeSpentToDate(
                        ticker.atTime(0, 0, 0, 0),
                        ticker.atTime(23, 59, 59, 59)
                );
            }
            return counter + getTimeSpentToDate(last.withDayOfYear(next.getDayOfYear()).toLocalDate().atTime(0, 0, 0, 0), next);
        }

    }

    private static long getTimeSpentToDate(LocalDateTime last, LocalDateTime next) {
        LocalDate date = last.toLocalDate();
        if (isNotWorkingDay(date)) {
            return 0L;
        }
        LocalDateTime lastWorkingDayStart = next.toLocalDate().atTime(WORKING_DAY_START_HOUR, WORKING_DAY_START_MINUTE, 0, 0);
        if (last.isBefore(lastWorkingDayStart)) {
            last = lastWorkingDayStart;
        }
        LocalDateTime nextWorkingDayEnd = next.toLocalDate().atTime(WORKING_DAY_END_HOUR, WORKING_DAY_END_MINUTE, 0, 0);
        if (next.isAfter(nextWorkingDayEnd)) {
            next = nextWorkingDayEnd;
        }
        if (next.isBefore(last)){
            next = last;
        }
        return Date.from(next.atZone(ZoneId.systemDefault()).toInstant()).getTime() - Date.from(last.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    private static boolean isNotWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return DayOfWeek.SUNDAY.equals(dayOfWeek) || DayOfWeek.SATURDAY.equals(dayOfWeek);
    }
}
