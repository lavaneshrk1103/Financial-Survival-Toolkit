package com.financialtoolkit.dashboard;

import java.math.BigDecimal;

public class CategoryTotal {
    private final String category;
    private final BigDecimal amount;

    public CategoryTotal(String category, BigDecimal amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
