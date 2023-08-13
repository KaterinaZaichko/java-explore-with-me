package ru.practicum;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeConstant {
    public final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
}
