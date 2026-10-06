package com.financialtoolkit.savings;

import com.financialtoolkit.common.BaseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsEntry extends BaseTransaction {
    public SavingsEntry(String id, BigDecimal amount, LocalDate date) {
        super(id, amount, date);
    }

    @Override
    public String getTransactionType() {
        return "SAVINGS";
    }
}
