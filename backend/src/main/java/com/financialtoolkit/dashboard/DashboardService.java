package com.financialtoolkit.dashboard;

import com.financialtoolkit.budget.BudgetService;
import com.financialtoolkit.challenge.ChallengeService;
import com.financialtoolkit.common.ExpenseFilter;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.expense.ExpenseService;
import com.financialtoolkit.insight.InsightService;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import com.financialtoolkit.recurring.RecurringExpenseService;
import com.financialtoolkit.savings.SavingsService;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DashboardService {
    private final ProfileService profileService;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final RecurringExpenseService recurringExpenseService;
    private final SavingsService savingsService;
    private final ChallengeService challengeService;
    private final InsightService insightService;
    private final ChartService chartService;
    private final ExecutorService dashboardExecutor = Executors.newFixedThreadPool(4);

    public DashboardService(ProfileService profileService, ExpenseService expenseService, BudgetService budgetService,
                            RecurringExpenseService recurringExpenseService, SavingsService savingsService,
                            ChallengeService challengeService, InsightService insightService, ChartService chartService) {
        this.profileService = profileService;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.recurringExpenseService = recurringExpenseService;
        this.savingsService = savingsService;
        this.challengeService = challengeService;
        this.insightService = insightService;
        this.chartService = chartService;
    }

    public DashboardSnapshot buildSnapshot(String profileId, ExpenseFilter filter) {
        Profile profile = profileService.getProfile(profileId);
        LocalDate today = LocalDate.now();
        recurringExpenseService.applyDueExpenses(profile, today);
        List<Expense> filteredExpenses = expenseService.filterExpenses(profile, filter);

        CompletableFuture<com.financialtoolkit.budget.BudgetSummary> budgetFuture =
                CompletableFuture.supplyAsync(() -> budgetService.calculateSummary(profile, today), dashboardExecutor);
        CompletableFuture<com.financialtoolkit.savings.SavingsSummary> savingsFuture =
                CompletableFuture.supplyAsync(() -> savingsService.calculateSummary(profile, today), dashboardExecutor);
        CompletableFuture<com.financialtoolkit.challenge.ChallengeStats> challengeFuture =
                CompletableFuture.supplyAsync(() -> challengeService.calculateStats(profile, today), dashboardExecutor);
        CompletableFuture<List<com.financialtoolkit.insight.Insight>> insightsFuture =
                CompletableFuture.supplyAsync(() -> insightService.generateInsights(profile, today), dashboardExecutor);
        CompletableFuture<List<CategoryTotal>> categoryTotalsFuture =
                CompletableFuture.supplyAsync(() -> chartService.categoryTotals(filteredExpenses), dashboardExecutor);
        CompletableFuture<List<com.financialtoolkit.recurring.BillCalendarItem>> billsFuture =
                CompletableFuture.supplyAsync(() -> recurringExpenseService.nextBills(profile, today, 30), dashboardExecutor);

        CompletableFuture.allOf(budgetFuture, savingsFuture, challengeFuture, insightsFuture, categoryTotalsFuture, billsFuture).join();
        return new DashboardSnapshot(
                budgetFuture.join(),
                savingsFuture.join(),
                challengeFuture.join(),
                insightsFuture.join(),
                categoryTotalsFuture.join(),
                billsFuture.join(),
                filteredExpenses
        );
    }

    @PreDestroy
    public void shutdown() {
        dashboardExecutor.shutdown();
    }
}
