package com.financialtoolkit.savings;

import java.math.BigDecimal;

public class SavingsSummary {
    private final BigDecimal totalSaved;
    private final BigDecimal remaining;
    private final BigDecimal currentMonthSaved;
    private final boolean goalComplete;

    public SavingsSummary(BigDecimal totalSaved, BigDecimal remaining, BigDecimal currentMonthSaved, boolean goalComplete) {
        this.totalSaved = totalSaved;
        this.remaining = remaining;
        this.currentMonthSaved = currentMonthSaved;
        this.goalComplete = goalComplete;
    }

    public BigDecimal getTotalSaved() {
        return totalSaved;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public BigDecimal getCurrentMonthSaved() {
        return currentMonthSaved;
    }

    public boolean isGoalComplete() {
        return goalComplete;
    }
}
