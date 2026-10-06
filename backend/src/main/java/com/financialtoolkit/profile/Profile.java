package com.financialtoolkit.profile;

import com.financialtoolkit.challenge.NoSpendChallenge;
import com.financialtoolkit.common.Identifiable;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.expense.Expense;
import com.financialtoolkit.recurring.RecurringRule;
import com.financialtoolkit.savings.SavingsEntry;
import com.financialtoolkit.savings.SavingsGoal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Identifiable<String> {
    private final String id;
    private BigDecimal budget;
    private final List<Expense> expenses;
    private final List<RecurringRule> recurringRules;
    private SavingsGoal goal;
    private final List<SavingsEntry> savingsEntries;
    private NoSpendChallenge challenge;

    public Profile(String id) {
        this(id, BigDecimal.ZERO);
    }

    public Profile(String id, BigDecimal budget) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("Profile name is required.");
        }
        this.id = id.trim();
        this.budget = MoneyUtils.normalize(budget);
        this.expenses = new ArrayList<>();
        this.recurringRules = new ArrayList<>();
        this.goal = new SavingsGoal();
        this.savingsEntries = new ArrayList<>();
        this.challenge = new NoSpendChallenge();
    }

    @Override
    public String getId() {
        return id;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = MoneyUtils.isNotPositive(budget) ? MoneyUtils.ZERO : MoneyUtils.normalize(budget);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<RecurringRule> getRecurringRules() {
        return recurringRules;
    }

    public SavingsGoal getGoal() {
        return goal;
    }

    public void setGoal(SavingsGoal goal) {
        this.goal = goal == null ? new SavingsGoal() : goal;
    }

    public List<SavingsEntry> getSavingsEntries() {
        return savingsEntries;
    }

    public NoSpendChallenge getChallenge() {
        return challenge;
    }

    public void setChallenge(NoSpendChallenge challenge) {
        this.challenge = challenge == null ? new NoSpendChallenge() : challenge;
    }
}
