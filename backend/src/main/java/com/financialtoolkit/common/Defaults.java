package com.financialtoolkit.common;

import com.financialtoolkit.expense.PaymentMethod;

public final class Defaults {
    public static final String[] PRESET_CATEGORIES = {"Food", "Travel", "Shopping"};
    public static final PaymentMethod[] PAYMENT_METHODS = PaymentMethod.values();

    private Defaults() {
    }
}
