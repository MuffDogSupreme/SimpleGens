package com.simplegens.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([tsmh])");

    public static long parseTime(String timeString) {
        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        if (!matcher.matches()) {
            return -1;
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        switch (unit) {
            case "t":
                return value;
            case "s":
                return value * 20;
            case "m":
                return value * 20 * 60;
            case "h":
                return value * 20 * 60 * 60;
            default:
                return -1;
        }
    }
}
