package com.financialtoolkit.common.api;

import com.financialtoolkit.budget.BudgetSummary;
import com.financialtoolkit.challenge.ChallengeStats;
import com.financialtoolkit.challenge.NoSpendChallenge;
import com.financialtoolkit.common.api.Responses.BillCalendarResponse;
import com.financialtoolkit.common.api.Responses.BudgetResponse;
import com.financialtoolkit.common.api.Responses.CategoryTotalResponse;
import com.financialtoolkit.common.api.Responses.ChallengeResponse;
import com.financialtoolkit.common.api.Responses.ChallengeStatsResponse;
import com.financialtoolkit.common.api.Responses.ExpenseResponse;
import com.financialtoolkit.common.api.Responses.InsightResponse;
import com.financialtoolkit.common.api.Responses.ProfileResponse;
import com.financialtoolkit.common.api.Responses.RecurringRuleResponse;
import com.financialtoolkit.common.api.Responses.SavingsEntryResponse;
import com.financialtoolkit.common.api.Responses.SavingsGoalResponse;
import com.financialtoolkit.common.api.Responses.SavingsSummaryResponse;
import com.financialtoolkit.dashboard.CategoryTotal;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.insight.Insight;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.recurring.BillCalendarItem;
import com.financialtoolkit.recurring.RecurringRule;
import com.financialtoolkit.savings.SavingsEntry;
import com.financialtoolkit.savings.SavingsGoal;
import com.financialtoolkit.savings.SavingsSummary;

import java.util.List;
import java.util.TreeSet;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static ProfileResponse profile(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getBudget(),
                profile.getExpenses().size(),
                profile.getRecurringRules().size(),
                profile.getSavingsEntries().size()
        );
    }

    public static ExpenseResponse expense(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getNote(),
                expense.getPaymentMethod().toDisplayName(),
                expense.getRecurringRuleId()
        );
    }

    public static BudgetResponse budget(BudgetSummary summary) {
        return new BudgetResponse(
                summary.getExpenseTotal(),
                summary.getRecurringCommitted(),
                summary.getTotal(),
                summary.getBudget(),
                summary.getUsedPercent(),
                summary.getProjectedSpend(),
                summary.getDailySafeSpend(),
                summary.getMessage(),
                summary.isExceeded()
        );
    }

    public static RecurringRuleResponse recurringRule(RecurringRule rule) {
        return new RecurringRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getAmount(),
                rule.getCategory(),
                rule.getPaymentMethod().toDisplayName(),
                rule.getDayOfMonth(),
                rule.getNote(),
                rule.getStartDate(),
                new TreeSet<>(rule.getAppliedPeriods())
        );
    }

    public static BillCalendarResponse bill(BillCalendarItem item) {
        return new BillCalendarResponse(item.getId(), item.getName(), item.getAmount(), item.getCategory(), item.getDueDate(), item.getDaysLeft());
    }

    public static SavingsGoalResponse savingsGoal(SavingsGoal goal) {
        return new SavingsGoalResponse(goal.getAmount(), goal.getDays(), goal.getStartDate(), goal.getDailyTarget(), goal.isConfigured());
    }

    public static SavingsEntryResponse savingsEntry(SavingsEntry entry) {
        return new SavingsEntryResponse(entry.getId(), entry.getAmount(), entry.getDate());
    }

    public static SavingsSummaryResponse savingsSummary(SavingsSummary summary) {
        return new SavingsSummaryResponse(summary.getTotalSaved(), summary.getRemaining(), summary.getCurrentMonthSaved(), summary.isGoalComplete());
    }

    public static ChallengeResponse challenge(NoSpendChallenge challenge) {
        return new ChallengeResponse(challenge.isActive(), challenge.getStartDate(), challenge.getEndDate(), challenge.getTargetDays(), challenge.isConfigured());
    }

    public static ChallengeStatsResponse challengeStats(ChallengeStats stats) {
        return new ChallengeStatsResponse(stats.getStreak(), stats.isActive(), stats.getStatus(), stats.getProgress());
    }

    public static InsightResponse insight(Insight insight) {
        return new InsightResponse(insight.getType(), insight.getText());
    }

    public static CategoryTotalResponse categoryTotal(CategoryTotal total) {
        return new CategoryTotalResponse(total.getCategory(), total.getAmount());
    }

    public static List<ExpenseResponse> expenses(List<Expense> expenses) {
        return expenses.stream().map(DtoMapper::expense).toList();
    }
}
