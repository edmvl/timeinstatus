package ru.itq.timeinstatus.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class WorkingDayUtilsTest {

    @Test
    public void testInOneDate() {
        LocalDate now = LocalDate.of(2023, 10, 25);
        Long timeSpent1 = WorkingDayUtils.getTimeSpent(
                now.atTime(0, 0, 0, 0),
                now.atTime(0, 0, 0, 0)
        );
        Assertions.assertEquals(0, timeSpent1);
        Long timeSpent2 = WorkingDayUtils.getTimeSpent(
                now.atTime(8, 0, 0, 0),
                now.atTime(20, 0, 0, 0)
        );
        Assertions.assertEquals(9 * 60 * 60 * 1000, timeSpent2);
        Long timeSpent3 = WorkingDayUtils.getTimeSpent(
                now.atTime(0, 0, 0, 0),
                now.atTime(23, 0, 0, 0)
        );
        Assertions.assertEquals(9 * 60 * 60 * 1000, timeSpent3);
        Long timeSpent4 = WorkingDayUtils.getTimeSpent(
                now.atTime(14, 0, 0, 0),
                now.atTime(23, 0, 0, 0)
        );
        Assertions.assertEquals(4 * 60 * 60 * 1000, timeSpent4);
    }

    @Test
    public void testInSeveralDate() {
        LocalDate now = LocalDate.of(2023, 10, 25);
        Long timeSpent1 = WorkingDayUtils.getTimeSpent(
                now.atTime(14, 0, 0, 0),
                now.atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(13 * 60 * 60 * 1000, timeSpent1);

        Long timeSpent2 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(1),
                now.atTime(23, 0, 0, 0)
        );
        Assertions.assertEquals(18 * 60 * 60 * 1000, timeSpent2);

        Long timeSpent3 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(1),
                now.atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(27 * 60 * 60 * 1000, timeSpent3);

        Long timeSpent4 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(2),
                now.atTime(23, 0, 0, 0).plusDays(1)
        );
        Assertions.assertEquals(36 * 60 * 60 * 1000, timeSpent4);

        Long timeSpent5 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(2),
                now.atTime(23, 0, 0, 0).plusDays(2)
        );
        Assertions.assertEquals(45 * 60 * 60 * 1000, timeSpent5);
    }

    @Test
    public void testNotWorkingDate() {
        LocalDate now = LocalDate.of(2023, 10, 25);
        Long timeSpent1 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(2),
                now.atTime(23, 0, 0, 0).plusDays(3)
        );
        Assertions.assertEquals(45 * 60 * 60 * 1000, timeSpent1);

        Long timeSpent2 = WorkingDayUtils.getTimeSpent(
                now.atTime(4, 0, 0, 0).minusDays(2),
                now.atTime(23, 0, 0, 0).plusDays(4)
        );
        Assertions.assertEquals(45 * 60 * 60 * 1000, timeSpent2);
    }

}