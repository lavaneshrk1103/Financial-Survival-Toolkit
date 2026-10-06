package com.financialtoolkit.dashboard;

import com.financialtoolkit.budget.BudgetSummary;
import com.financialtoolkit.challenge.ChallengeStats;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.insight.Insight;
import com.financialtoolkit.recurring.BillCalendarItem;
import com.financialtoolkit.savings.SavingsSummary;

import java.util.List;

public class DashboardSnapshot {
    private final BudgetSummary budgetSummary;
    private final SavingsSummary savingsSummary;
    private final ChallengeStats challengeStats;
    private final List<Insight> insights;
    private final List<CategoryTotal> categoryTotals;
    private final List<BillCalendarItem> upcomingBills;
    private final List<Expense> recentExpenses;

    public DashboardSnapshot(BudgetSummary budgetSummary, SavingsSummary savingsSummary, ChallengeStats challengeStats,
                             List<Insight> insights, List<CategoryTotal> categoryTotals, List<BillCalendarItem> upcomingBills,
                             List<Expense> recentExpenses) {
        this.budgetSummary = budgetSummary;
        this.savingsSummary = savingsSummary;
        this.challengeStats = challengeStats;
        this.insights = insights;
        this.categoryTotals = categoryTotals;
        this.upcomingBills = upcomingBills;
        this.recentExpenses = recentExpenses;
    }

    public BudgetSummary getBudgetSummary() {
        return budgetSummary;
    }

    public SavingsSummary getSavingsSummary() {
        return savingsSummary;
    }

    public ChallengeStats getChallengeStats() {
        return challengeStats;
    }

    public List<Insight> getInsights() {
        return insights;
    }

    public List<CategoryTotal> getCategoryTotals() {
        return categoryTotals;
    }

    public List<BillCalendarItem> getUpcomingBills() {
        return upcomingBills;
    }

    public List<Expense> getRecentExpenses() {
        return recentExpenses;
    }
}
