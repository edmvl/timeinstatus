package ru.itq.timeinstatus.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

class WorkingDayUtilsTest {

    @Test
    public void testInOneDate() {
        Long timeSpent1 = WorkingDayUtils.getTimeSpent(LocalDateTime.now(), LocalDateTime.now());
        Assertions.assertEquals(0, timeSpent1);
        Long timeSpent2 = WorkingDayUtils.getTimeSpent(LocalDateTime.now().withHour(8), LocalDateTime.now().withHour(20));
        Assertions.assertEquals(9 * 60 * 60 * 1000, timeSpent2);
        Long timeSpent3 = WorkingDayUtils.getTimeSpent(LocalDateTime.now().withHour(0), LocalDateTime.now().withHour(23));
        Assertions.assertEquals(9 * 60 * 60 * 1000, timeSpent3);
        Long timeSpent4 = WorkingDayUtils.getTimeSpent(
                LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().withHour(23)
        );
        Assertions.assertEquals(4 * 60 * 60 * 1000, timeSpent4);
    }

    @Test
    public void testInSeveralDate() {
        Long timeSpent1 = WorkingDayUtils.getTimeSpent(
                LocalDate.now().atTime(14, 0, 0, 0),
                LocalDate.now().atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(13 * 60 * 60 * 1000, timeSpent1);

        Long timeSpent2 = WorkingDayUtils.getTimeSpent(
                LocalDate.now().atTime(4, 0, 0, 0).minusDays(1),
                LocalDate.now().atTime(23, 0, 0, 0)
        );
        Assertions.assertEquals(18 * 60 * 60 * 1000, timeSpent2);

        Long timeSpent3 = WorkingDayUtils.getTimeSpent(
                LocalDate.now().atTime(4, 0, 0, 0).minusDays(1),
                LocalDate.now().atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(27 * 60 * 60 * 1000, timeSpent3);

        Long timeSpent4 = WorkingDayUtils.getTimeSpent(
                LocalDate.now().atTime(4, 0, 0, 0).minusDays(2),
                LocalDate.now().atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(36 * 60 * 60 * 1000, timeSpent4);

        Long timeSpent5 = WorkingDayUtils.getTimeSpent(
                LocalDate.now().atTime(4, 0, 0, 0).minusDays(2),
                LocalDate.now().atTime(23, 0, 0, 0).plusDays(2)
        );
        Assertions.assertEquals(45 * 60 * 60 * 1000, timeSpent5);
    }

}