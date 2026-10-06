package com.financialtoolkit.expense;

import com.financialtoolkit.common.BaseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense extends BaseTransaction {
    private String category;
    private String note;
    private PaymentMethod paymentMethod;
    private String recurringRuleId;

    public Expense(String id, BigDecimal amount, String category, LocalDate date, String note, PaymentMethod paymentMethod) {
        this(id, amount, category, date, note, paymentMethod, "");
    }

    public Expense(String id, BigDecimal amount, String category, LocalDate date, String note, PaymentMethod paymentMethod, String recurringRuleId) {
        super(id, amount, date);
        this.category = category == null || category.isBlank() ? "Other" : category.trim();
        this.note = note == null ? "" : note.trim();
        this.paymentMethod = paymentMethod == null ? PaymentMethod.UPI : paymentMethod;
        this.recurringRuleId = recurringRuleId == null ? "" : recurringRuleId;
    }

    @Override
    public String getTransactionType() {
        return "EXPENSE";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null || category.isBlank() ? "Other" : category.trim();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note.trim();
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod == null ? PaymentMethod.UPI : paymentMethod;
    }

    public String getRecurringRuleId() {
        return recurringRuleId;
    }

    public void setRecurringRuleId(String recurringRuleId) {
        this.recurringRuleId = recurringRuleId == null ? "" : recurringRuleId;
    }
}
