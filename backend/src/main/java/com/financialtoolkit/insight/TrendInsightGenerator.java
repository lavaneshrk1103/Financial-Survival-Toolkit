package com.financialtoolkit.insight;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class TrendInsightGenerator implements InsightGenerator {
    @Override
    public List<Insight> generate(Profile profile, LocalDate today) {
        String currentMonth = DateUtils.monthKey(today);
        String previousMonth = DateUtils.monthKey(today.minusMonths(1));
        BigDecimal currentTotal = totalForMonth(profile, currentMonth);
        BigDecimal previousTotal = totalForMonth(profile, previousMonth);
        if (previousTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        double deltaPercent = currentTotal.subtract(previousTotal)
                .divide(previousTotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        if (deltaPercent >= 15) {
            return List.of(new Insight("warning", "Spending is up %.1f%% vs last month.".formatted(deltaPercent)));
        }
        if (deltaPercent <= -10) {
            return List.of(new Insight("info", "Good trend: spending is down %.1f%% vs last month.".formatted(Math.abs(deltaPercent))));
        }
        return List.of();
    }

    private BigDecimal totalForMonth(Profile profile, String month) {
        return profile.getExpenses().stream()
                .filter(expense -> DateUtils.monthKey(expense.getDate()).equals(month))
                .map(Expense::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
    }
}
