package com.financialtoolkit.savings;

import com.financialtoolkit.common.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class SavingsGoal {
    private BigDecimal amount;
    private int days;
    private LocalDate startDate;
    private BigDecimal dailyTarget;

    public SavingsGoal() {
        this(BigDecimal.ZERO, 0, null);
    }

    public SavingsGoal(BigDecimal amount, int days, LocalDate startDate) {
        this.amount = MoneyUtils.normalize(amount);
        this.days = days;
        this.startDate = startDate;
        this.dailyTarget = days > 0
                ? MoneyUtils.normalize(this.amount.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP))
                : MoneyUtils.ZERO;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getDays() {
        return days;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public BigDecimal getDailyTarget() {
        return dailyTarget;
    }

    public boolean isConfigured() {
        return amount.compareTo(BigDecimal.ZERO) > 0 && days > 0;
    }
}
