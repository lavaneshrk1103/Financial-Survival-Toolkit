package com.financialtoolkit.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    public static final int MONEY_SCALE = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        return amount == null ? ZERO : amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static boolean isNotPositive(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return normalize(normalize(left).add(normalize(right)));
    }
}
