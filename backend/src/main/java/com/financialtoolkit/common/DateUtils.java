package com.financialtoolkit.common;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public final class DateUtils {
    private DateUtils() {
    }

    public static String monthKey(LocalDate date) {
        return "%d-%02d".formatted(date.getYear(), date.getMonthValue());
    }

    public static LocalDate dueDateForMonth(int dayOfMonth, YearMonth month) {
        int dueDay = Math.min(Math.max(dayOfMonth, 1), month.lengthOfMonth());
        return month.atDay(dueDay);
    }

    public static long daysBetweenInclusive(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
