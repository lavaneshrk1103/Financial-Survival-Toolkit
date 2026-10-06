package com.financialtoolkit.common;

import java.util.UUID;

public final class IdGenerator {
    public static final String EXPENSE_PREFIX = "exp";
    public static final String RECURRING_PREFIX = "rec";
    public static final String SAVINGS_PREFIX = "sav";

    private IdGenerator() {
    }

    public static String expenseId() {
        return prefixed(EXPENSE_PREFIX);
    }

    public static String recurringId() {
        return prefixed(RECURRING_PREFIX);
    }

    public static String savingsId() {
        return prefixed(SAVINGS_PREFIX);
    }

    private static String prefixed(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
