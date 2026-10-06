package com.financialtoolkit.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillCalendarItem {
    private final String id;
    private final String name;
    private final BigDecimal amount;
    private final String category;
    private final LocalDate dueDate;
    private final long daysLeft;

    public BillCalendarItem(String id, String name, BigDecimal amount, String category, LocalDate dueDate, long daysLeft) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.dueDate = dueDate;
        this.daysLeft = daysLeft;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public long getDaysLeft() {
        return daysLeft;
    }
}
