package com.financialtoolkit;

import com.financialtoolkit.budget.BudgetService;
import com.financialtoolkit.budget.BudgetSummary;
import com.financialtoolkit.challenge.ChallengeService;
import com.financialtoolkit.challenge.ChallengeStats;
import com.financialtoolkit.challenge.NoSpendChallenge;
import com.financialtoolkit.common.ExpenseFilter;
import com.financialtoolkit.common.IdGenerator;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.expense.ExpenseRequest;
import com.financialtoolkit.expense.ExpenseService;
import com.financialtoolkit.expense.PaymentMethod;
import com.financialtoolkit.insight.CategoryShareInsightGenerator;
import com.financialtoolkit.insight.Insight;
import com.financialtoolkit.insight.InsightService;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileRepository;
import com.financialtoolkit.profile.ProfileService;
import com.financialtoolkit.recurring.RecurringExpenseService;
import com.financialtoolkit.recurring.RecurringRule;
import com.financialtoolkit.savings.SavingsGoal;
import com.financialtoolkit.savings.SavingsService;
import com.financialtoolkit.savings.SavingsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialToolkitBusinessTest {
    private ProfileService profileService;
    private ExpenseService expenseService;
    private BudgetService budgetService;
    private RecurringExpenseService recurringExpenseService;
    private SavingsService savingsService;
    private ChallengeService challengeService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(new ProfileRepository());
        expenseService = new ExpenseService(profileService);
        budgetService = new BudgetService(profileService);
        recurringExpenseService = new RecurringExpenseService(profileService, expenseService);
        savingsService = new SavingsService(profileService);
        challengeService = new ChallengeService(profileService);
    }

    @Test
    void expenseValidationRejectsNegativeAmounts() {
        profileService.createProfile("Personal");

        ExpenseRequest request = new ExpenseRequest(
                BigDecimal.valueOf(-50),
                "Food",
                LocalDate.of(2026, 10, 6),
                "Lunch",
                PaymentMethod.UPI
        );

        assertThrows(ValidationException.class, () -> expenseService.addExpense("Personal", request));
    }

    @Test
    void expenseFilteringMatchesCategorySearchAndDates() {
        Profile profile = profileService.createProfile("Personal");
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(120),
                "Food",
                LocalDate.of(2026, 10, 6),
                "College lunch",
                PaymentMethod.UPI
        ));
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(900),
                "Travel",
                LocalDate.of(2026, 9, 30),
                "Bus pass",
                PaymentMethod.CARD
        ));

        List<Expense> filtered = expenseService.filterExpenses(
                profile,
                new ExpenseFilter("lunch", "Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))
        );

        assertEquals(1, filtered.size());
        assertEquals("College lunch", filtered.get(0).getNote());
    }

    @Test
    void budgetSummaryIncludesUnappliedRecurringCommitments() {
        Profile profile = profileService.createProfile("Personal");
        profile.setBudget(BigDecimal.valueOf(1000));
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(200),
                "Food",
                LocalDate.of(2026, 10, 6),
                "",
                PaymentMethod.UPI
        ));
        profile.getRecurringRules().add(new RecurringRule(
                IdGenerator.recurringId(),
                "Rent",
                BigDecimal.valueOf(300),
                PaymentMethod.BANK_TRANSFER,
                30,
                LocalDate.of(2026, 10, 1)
        ));

        BudgetSummary summary = budgetService.calculateSummary(profile, LocalDate.of(2026, 10, 6));

        assertEquals(new BigDecimal("500.00"), summary.getTotal());
        assertEquals(new BigDecimal("300.00"), summary.getRecurringCommitted());
        assertEquals(50.0, summary.getUsedPercent(), 0.0001);
    }

    @Test
    void recurringApplicationIsIdempotentAcrossMonths() {
        Profile profile = profileService.createProfile("Personal");
        profile.getRecurringRules().add(new RecurringRule(
                IdGenerator.recurringId(),
                "Rent",
                BigDecimal.valueOf(100),
                PaymentMethod.BANK_TRANSFER,
                1,
                LocalDate.of(2026, 1, 1)
        ));

        boolean firstRunChanged = recurringExpenseService.applyDueExpenses(profile, LocalDate.of(2026, 3, 2));
        boolean secondRunChanged = recurringExpenseService.applyDueExpenses(profile, LocalDate.of(2026, 3, 2));

        assertTrue(firstRunChanged);
        assertFalse(secondRunChanged);
        assertEquals(3, profile.getExpenses().size());
        assertEquals(3, profile.getRecurringRules().get(0).getAppliedPeriods().size());
    }

    @Test
    void savingsSummaryCalculatesRemainingAndMonthSaved() {
        Profile profile = profileService.createProfile("Personal");
        profile.setGoal(new SavingsGoal(BigDecimal.valueOf(500), 5, LocalDate.of(2026, 10, 1)));
        savingsService.addSavingsEntry(profile, BigDecimal.valueOf(125), LocalDate.of(2026, 10, 2));
        savingsService.addSavingsEntry(profile, BigDecimal.valueOf(75), LocalDate.of(2026, 10, 3));

        SavingsSummary summary = savingsService.calculateSummary(profile, LocalDate.of(2026, 10, 6));

        assertEquals(new BigDecimal("200.00"), summary.getTotalSaved());
        assertEquals(new BigDecimal("300.00"), summary.getRemaining());
        assertEquals(new BigDecimal("200.00"), summary.getCurrentMonthSaved());
        assertFalse(summary.isGoalComplete());
    }

    @Test
    void challengeStatsCalculateStreakAndProgress() {
        Profile profile = profileService.createProfile("Personal");
        profile.setChallenge(new NoSpendChallenge(
                true,
                LocalDate.of(2026, 10, 4),
                LocalDate.of(2026, 10, 6),
                3
        ));
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(50),
                "Food",
                LocalDate.of(2026, 10, 4),
                "",
                PaymentMethod.CASH
        ));

        ChallengeStats stats = challengeService.calculateStats(profile, LocalDate.of(2026, 10, 6));

        assertEquals(2, stats.getStreak());
        assertEquals("2/3 no-spend days (66.7%).", stats.getProgress());
        assertTrue(stats.isActive());
    }

    @Test
    void insightsUseStrategyGenerators() {
        Profile profile = profileService.createProfile("Personal");
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(900),
                "Food",
                LocalDate.of(2026, 10, 6),
                "",
                PaymentMethod.UPI
        ));
        expenseService.addExpense("Personal", new ExpenseRequest(
                BigDecimal.valueOf(100),
                "Travel",
                LocalDate.of(2026, 10, 6),
                "",
                PaymentMethod.CARD
        ));
        InsightService insightService = new InsightService(List.of(new CategoryShareInsightGenerator()));

        List<Insight> insights = insightService.generateInsights(profile, LocalDate.of(2026, 10, 6));

        assertEquals(1, insights.size());
        assertEquals("warning", insights.get(0).getType());
        assertTrue(insights.get(0).getText().contains("Food"));
    }
}
