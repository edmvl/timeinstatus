package ru.itq.timeinstatus.utils;

import java.util.Objects;

public class TimeFormatter {
    public static String formatTime(Long milliseconds) {
        if (Objects.isNull(milliseconds) || milliseconds == 0) {
            return "";
        }
        long seconds = milliseconds / 1000;
        long d = seconds / 32400;
        long h = seconds / 3600 - d * 9;
        long m = (seconds % 3600) / 60;
        return addLeadingZero(d) + ":" + addLeadingZero(h) + ":" + addLeadingZero(m);
    }

    public static String addLeadingZero(long d) {
        return d > 9 ? "" + d : "0" + d;
    }


}
