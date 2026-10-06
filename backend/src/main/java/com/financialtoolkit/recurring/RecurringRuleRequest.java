package com.financialtoolkit.recurring;

import com.financialtoolkit.expense.PaymentMethod;

import java.math.BigDecimal;

public class RecurringRuleRequest {
    private BigDecimal amount;
    private String name;
    private PaymentMethod paymentMethod;
    private int dayOfMonth;

    public RecurringRuleRequest() {
    }

    public RecurringRuleRequest(String name, BigDecimal amount, PaymentMethod paymentMethod, int dayOfMonth) {
        this.name = name;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.dayOfMonth = dayOfMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }
}
