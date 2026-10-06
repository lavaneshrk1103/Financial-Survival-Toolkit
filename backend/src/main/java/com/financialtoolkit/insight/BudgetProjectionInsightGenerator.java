package com.financialtoolkit.insight;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class BudgetProjectionInsightGenerator implements InsightGenerator {
    @Override
    public List<Insight> generate(Profile profile, LocalDate today) {
        if (profile.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        String currentMonth = DateUtils.monthKey(today);
        BigDecimal currentMonthTotal = profile.getExpenses().stream()
                .filter(expense -> DateUtils.monthKey(expense.getDate()).equals(currentMonth))
                .map(Expense::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
        BigDecimal projected = currentMonthTotal
                .divide(BigDecimal.valueOf(Math.max(today.getDayOfMonth(), 1)), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(today.lengthOfMonth()));
        if (projected.compareTo(profile.getBudget().multiply(BigDecimal.valueOf(1.1))) > 0) {
            return List.of(new Insight("risk", "At current pace, projected monthly spend is " + MoneyUtils.normalize(projected) + "."));
        }
        return List.of();
    }
}
