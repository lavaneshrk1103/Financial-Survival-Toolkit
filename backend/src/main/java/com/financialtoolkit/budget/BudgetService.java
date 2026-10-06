package com.financialtoolkit.budget;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class BudgetService {
    private final ProfileService profileService;

    public BudgetService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public Profile setBudget(String profileId, BigDecimal budget) {
        Profile profile = profileService.getProfile(profileId);
        profile.setBudget(budget);
        return profile;
    }

    public BudgetSummary calculateSummary(Profile profile, LocalDate today) {
        BigDecimal expenseTotal = profile.getExpenses().stream()
                .map(expense -> expense.getAmount())
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);

        String currentMonth = DateUtils.monthKey(today);
        BigDecimal recurringCommitted = profile.getRecurringRules().stream()
                .filter(rule -> !rule.hasAppliedPeriod(currentMonth))
                .map(rule -> rule.getAmount())
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);

        BigDecimal total = MoneyUtils.add(expenseTotal, recurringCommitted);
        BigDecimal budget = profile.getBudget();
        if (budget.compareTo(BigDecimal.ZERO) <= 0) {
            return new BudgetSummary(expenseTotal, recurringCommitted, total, budget, 0, MoneyUtils.ZERO,
                    MoneyUtils.ZERO, "No monthly budget set yet.", false);
        }

        double usedPercent = total.divide(budget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        int day = today.getDayOfMonth();
        int daysInMonth = today.lengthOfMonth();
        int remainingDays = Math.max(daysInMonth - day, 0);
        BigDecimal projected = total.divide(BigDecimal.valueOf(Math.max(day, 1)), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(daysInMonth));
        BigDecimal remainingBudget = budget.subtract(total);
        BigDecimal dailySafeSpend = remainingDays > 0
                ? remainingBudget.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP)
                : remainingBudget;
        boolean exceeded = total.compareTo(budget) > 0;
        String message = exceeded ? "Budget exceeded." : "Budget left: " + MoneyUtils.normalize(remainingBudget);

        return new BudgetSummary(expenseTotal, recurringCommitted, MoneyUtils.normalize(total), budget, usedPercent,
                MoneyUtils.normalize(projected), MoneyUtils.normalize(dailySafeSpend.max(BigDecimal.ZERO)), message, exceeded);
    }
}
