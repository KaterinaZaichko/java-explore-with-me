package ru.practicum;

import lombok.experimental.UtilityClass;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeConstant {
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
}
