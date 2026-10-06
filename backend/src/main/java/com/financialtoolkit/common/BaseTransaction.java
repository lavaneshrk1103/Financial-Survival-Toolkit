package com.financialtoolkit.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public abstract class BaseTransaction implements Identifiable<String> {
    private final String id;
    private BigDecimal amount;
    private LocalDate date;

    protected BaseTransaction(String id, BigDecimal amount, LocalDate date) {
        this.id = Objects.requireNonNull(id, "id is required");
        setAmount(amount);
        this.date = Objects.requireNonNull(date, "date is required");
    }

    @Override
    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (MoneyUtils.isNotPositive(amount)) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        this.amount = MoneyUtils.normalize(amount);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "date is required");
    }

    public abstract String getTransactionType();
}
