package com.financialtoolkit.insight;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CategoryShareInsightGenerator implements InsightGenerator {
    @Override
    public List<Insight> generate(Profile profile, LocalDate today) {
        String currentMonth = DateUtils.monthKey(today);
        Map<String, BigDecimal> categoryTotals = profile.getExpenses().stream()
                .filter(expense -> DateUtils.monthKey(expense.getDate()).equals(currentMonth))
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(MoneyUtils.ZERO, Expense::getAmount, MoneyUtils::add)
                ));
        BigDecimal monthTotal = categoryTotals.values().stream().reduce(MoneyUtils.ZERO, MoneyUtils::add);
        if (monthTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        Map.Entry<String, BigDecimal> topCategory = categoryTotals.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);
        if (topCategory == null) {
            return List.of();
        }

        double share = topCategory.getValue()
                .divide(monthTotal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        if (share >= 45) {
            return List.of(new Insight("warning", "%s is %.1f%% of this month's spend.".formatted(topCategory.getKey(), share)));
        }
        return List.of();
    }
}
