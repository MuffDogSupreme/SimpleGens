package com.simplegens.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeParserTest {

    @Test
    void parseTicksReturnsSameValue() {
        assertEquals(15, TimeParser.parseTime("15t"));
        assertEquals(7, TimeParser.parseTime("7T"));
    }

    @Test
    void parseSecondsReturnsTicks() {
        assertEquals(20, TimeParser.parseTime("1s"));
        assertEquals(400, TimeParser.parseTime("20s"));
    }

    @Test
    void parseMinutesReturnsTicks() {
        assertEquals(1200, TimeParser.parseTime("1m"));
        assertEquals(2400, TimeParser.parseTime("2M"));
    }

    @Test
    void parseHoursReturnsTicks() {
        assertEquals(72000, TimeParser.parseTime("1h"));
        assertEquals(360000, TimeParser.parseTime("5H"));
    }

    @Test
    void parseInvalidFormatReturnsNegativeOne() {
        assertEquals(-1, TimeParser.parseTime("nope"));
        assertEquals(-1, TimeParser.parseTime("10x"));
        assertEquals(-1, TimeParser.parseTime(""));
    }
}
