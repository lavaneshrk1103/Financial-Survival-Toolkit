package com.financialtoolkit.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {
    private BigDecimal amount;
    private String category;
    private LocalDate date;
    private String note;
    private PaymentMethod paymentMethod;

    public ExpenseRequest() {
    }

    public ExpenseRequest(BigDecimal amount, String category, LocalDate date, String note, PaymentMethod paymentMethod) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
}
