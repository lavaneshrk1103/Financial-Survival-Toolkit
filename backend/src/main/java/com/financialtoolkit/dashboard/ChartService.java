package com.financialtoolkit.dashboard;

import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.expense.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChartService {
    public List<CategoryTotal> categoryTotals(List<Expense> expenses) {
        Map<String, BigDecimal> totals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(MoneyUtils.ZERO, Expense::getAmount, MoneyUtils::add)
                ));

        return totals.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
                .toList();
    }
}
