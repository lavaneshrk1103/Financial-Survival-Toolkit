package com.financialtoolkit.insight;

import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class AnomalyInsightGenerator implements InsightGenerator {
    @Override
    public List<Insight> generate(Profile profile, LocalDate today) {
        if (profile.getExpenses().size() < 5) {
            return List.of();
        }
        List<Expense> sorted = profile.getExpenses().stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .toList();
        Expense latest = sorted.get(0);
        List<Expense> baseline = sorted.stream().skip(1).limit(10).toList();
        BigDecimal baselineTotal = baseline.stream()
                .map(Expense::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
        BigDecimal average = baselineTotal.divide(BigDecimal.valueOf(Math.max(baseline.size(), 1)), 2, java.math.RoundingMode.HALF_UP);
        if (latest.getAmount().compareTo(average.multiply(BigDecimal.valueOf(2.2))) > 0
                && latest.getAmount().compareTo(BigDecimal.valueOf(500)) > 0) {
            return List.of(new Insight("risk", "Anomaly detected: latest expense (" + latest.getAmount() + ") is much higher than recent average."));
        }
        return List.of();
    }
}
