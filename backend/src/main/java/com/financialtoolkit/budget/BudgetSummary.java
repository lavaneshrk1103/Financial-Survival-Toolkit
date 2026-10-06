package com.financialtoolkit.budget;

import java.math.BigDecimal;

public class BudgetSummary {
    private final BigDecimal expenseTotal;
    private final BigDecimal recurringCommitted;
    private final BigDecimal total;
    private final BigDecimal budget;
    private final double usedPercent;
    private final BigDecimal projectedSpend;
    private final BigDecimal dailySafeSpend;
    private final String message;
    private final boolean exceeded;

    public BudgetSummary(BigDecimal expenseTotal, BigDecimal recurringCommitted, BigDecimal total, BigDecimal budget,
                         double usedPercent, BigDecimal projectedSpend, BigDecimal dailySafeSpend,
                         String message, boolean exceeded) {
        this.expenseTotal = expenseTotal;
        this.recurringCommitted = recurringCommitted;
        this.total = total;
        this.budget = budget;
        this.usedPercent = usedPercent;
        this.projectedSpend = projectedSpend;
        this.dailySafeSpend = dailySafeSpend;
        this.message = message;
        this.exceeded = exceeded;
    }

    public BigDecimal getExpenseTotal() {
        return expenseTotal;
    }

    public BigDecimal getRecurringCommitted() {
        return recurringCommitted;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public double getUsedPercent() {
        return usedPercent;
    }

    public BigDecimal getProjectedSpend() {
        return projectedSpend;
    }

    public BigDecimal getDailySafeSpend() {
        return dailySafeSpend;
    }

    public String getMessage() {
        return message;
    }

    public boolean isExceeded() {
        return exceeded;
    }
}
