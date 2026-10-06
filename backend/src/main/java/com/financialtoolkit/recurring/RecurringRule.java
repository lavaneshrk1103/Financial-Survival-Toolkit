package com.financialtoolkit.recurring;

import com.financialtoolkit.common.Identifiable;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.expense.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RecurringRule implements Identifiable<String> {
    private final String id;
    private String name;
    private BigDecimal amount;
    private String category;
    private PaymentMethod paymentMethod;
    private int dayOfMonth;
    private String note;
    private LocalDate startDate;
    private final Set<String> appliedPeriods;

    public RecurringRule(String id, String name, BigDecimal amount, PaymentMethod paymentMethod, int dayOfMonth, LocalDate startDate) {
        this(id, name, amount, "Recurring", paymentMethod, dayOfMonth, name, startDate, new HashSet<>());
    }

    public RecurringRule(String id, String name, BigDecimal amount, String category, PaymentMethod paymentMethod,
                         int dayOfMonth, String note, LocalDate startDate, Set<String> appliedPeriods) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = requireText(name, "Recurring name is required.");
        setAmount(amount);
        this.category = category == null || category.isBlank() ? "Recurring" : category.trim();
        this.paymentMethod = paymentMethod == null ? PaymentMethod.UPI : paymentMethod;
        setDayOfMonth(dayOfMonth);
        this.note = note == null ? "" : note.trim();
        this.startDate = startDate == null ? LocalDate.now() : startDate;
        this.appliedPeriods = appliedPeriods == null ? new HashSet<>() : new HashSet<>(appliedPeriods);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (MoneyUtils.isNotPositive(amount)) {
            throw new ValidationException("Recurring amount must be greater than 0.");
        }
        this.amount = MoneyUtils.normalize(amount);
    }

    public String getCategory() {
        return category;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new ValidationException("Due day must be between 1 and 31.");
        }
        this.dayOfMonth = dayOfMonth;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Set<String> getAppliedPeriods() {
        return appliedPeriods;
    }

    public boolean hasAppliedPeriod(String period) {
        return appliedPeriods.contains(period);
    }

    public void markApplied(String period) {
        appliedPeriods.add(period);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
