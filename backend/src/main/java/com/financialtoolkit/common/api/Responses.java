package com.financialtoolkit.common.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public final class Responses {
    private Responses() {
    }

    public record ProfileResponse(String id, BigDecimal budget, int expenseCount, int recurringRuleCount, int savingsEntryCount) {
    }

    public record ExpenseResponse(String id, BigDecimal amount, String category, LocalDate date, String note,
                                  String paymentMethod, String recurringId) {
    }

    public record BudgetResponse(BigDecimal expenseTotal, BigDecimal recurringCommitted, BigDecimal total,
                                 BigDecimal budget, double usedPercent, BigDecimal projectedSpend,
                                 BigDecimal dailySafeSpend, String message, boolean exceeded) {
    }

    public record RecurringRuleResponse(String id, String name, BigDecimal amount, String category,
                                        String paymentMethod, int dayOfMonth, String note, LocalDate startDate,
                                        Set<String> appliedPeriods) {
    }

    public record BillCalendarResponse(String id, String name, BigDecimal amount, String category,
                                       LocalDate dueDate, long daysLeft) {
    }

    public record SavingsGoalResponse(BigDecimal amount, int days, LocalDate startDate, BigDecimal dailyTarget,
                                      boolean configured) {
    }

    public record SavingsEntryResponse(String id, BigDecimal amount, LocalDate date) {
    }

    public record SavingsSummaryResponse(BigDecimal totalSaved, BigDecimal remaining, BigDecimal currentMonthSaved,
                                         boolean goalComplete) {
    }

    public record ChallengeResponse(boolean active, LocalDate startDate, LocalDate endDate, int targetDays,
                                    boolean configured) {
    }

    public record ChallengeStatsResponse(int streak, boolean active, String status, String progress) {
    }

    public record ChallengeDetailsResponse(ChallengeResponse challenge, ChallengeStatsResponse stats) {
    }

    public record InsightResponse(String type, String text) {
    }

    public record CategoryTotalResponse(String category, BigDecimal amount) {
    }

    public record DashboardResponse(String profileId, List<ExpenseResponse> recentExpenses,
                                    List<RecurringRuleResponse> recurringRules, BigDecimal recurringMonthlyTotal,
                                    List<BillCalendarResponse> upcomingBills, BudgetResponse budget,
                                    SavingsGoalResponse savingsGoal, List<SavingsEntryResponse> savingsEntries,
                                    SavingsSummaryResponse savingsSummary, ChallengeResponse challenge,
                                    ChallengeStatsResponse challengeStats, List<CategoryTotalResponse> categoryTotals,
                                    List<InsightResponse> insights, List<InsightResponse> alerts) {
    }
}
