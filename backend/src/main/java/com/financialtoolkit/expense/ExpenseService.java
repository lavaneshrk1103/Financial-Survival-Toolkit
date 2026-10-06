package com.financialtoolkit.expense;

import com.financialtoolkit.common.ExpenseFilter;
import com.financialtoolkit.common.IdGenerator;
import com.financialtoolkit.common.ResourceNotFoundException;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import com.financialtoolkit.recurring.RecurringRule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ExpenseService {
    private final ProfileService profileService;

    public ExpenseService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public Expense addExpense(String profileId, ExpenseRequest request) {
        return addExpense(profileService.getProfile(profileId), request);
    }

    public Expense addExpense(Profile profile, ExpenseRequest request) {
        validateExpenseRequest(request);
        Expense expense = new Expense(
                IdGenerator.expenseId(),
                request.getAmount(),
                request.getCategory(),
                request.getDate() == null ? LocalDate.now() : request.getDate(),
                request.getNote(),
                request.getPaymentMethod()
        );
        synchronized (profile) {
            profile.getExpenses().add(expense);
        }
        return expense;
    }

    public Expense addExpense(Profile profile, RecurringRule rule, LocalDate dueDate) {
        Expense expense = new Expense(
                IdGenerator.expenseId(),
                rule.getAmount(),
                rule.getCategory(),
                dueDate,
                "Auto: " + rule.getName(),
                rule.getPaymentMethod(),
                rule.getId()
        );
        synchronized (profile) {
            profile.getExpenses().add(expense);
        }
        return expense;
    }

    public Expense updateExpense(String profileId, String expenseId, ExpenseRequest request) {
        validateExpenseRequest(request);
        Profile profile = profileService.getProfile(profileId);
        synchronized (profile) {
            Expense expense = findExpense(profile, expenseId);
            expense.setAmount(request.getAmount());
            expense.setCategory(request.getCategory());
            expense.setDate(request.getDate() == null ? LocalDate.now() : request.getDate());
            expense.setNote(request.getNote());
            expense.setPaymentMethod(request.getPaymentMethod());
            return expense;
        }
    }

    public void deleteExpense(String profileId, String expenseId) {
        Profile profile = profileService.getProfile(profileId);
        synchronized (profile) {
            boolean removed = profile.getExpenses().removeIf(expense -> expense.getId().equals(expenseId));
            if (!removed) {
                throw new ResourceNotFoundException("Expense not found.");
            }
        }
    }

    public List<Expense> filterExpenses(Profile profile, ExpenseFilter filter) {
        ExpenseFilter effectiveFilter = filter == null ? new ExpenseFilter() : filter;
        String searchTerm = effectiveFilter.getSearchTerm().toLowerCase(Locale.ROOT).trim();
        String selectedCategory = effectiveFilter.getCategory();

        return profile.getExpenses().stream()
                .filter(expense -> "All".equals(selectedCategory) || expense.getCategory().equals(selectedCategory))
                .filter(expense -> effectiveFilter.getStartDate() == null || !expense.getDate().isBefore(effectiveFilter.getStartDate()))
                .filter(expense -> effectiveFilter.getEndDate() == null || !expense.getDate().isAfter(effectiveFilter.getEndDate()))
                .filter(expense -> {
                    if (searchTerm.isBlank()) return true;
                    String haystack = (expense.getCategory() + " " + expense.getNote() + " " + expense.getPaymentMethod().toDisplayName())
                            .toLowerCase(Locale.ROOT);
                    return haystack.contains(searchTerm);
                })
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .toList();
    }

    private Expense findExpense(Profile profile, String expenseId) {
        return profile.getExpenses().stream()
                .filter(expense -> expense.getId().equals(expenseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found."));
    }

    private void validateExpenseRequest(ExpenseRequest request) {
        if (request == null) {
            throw new ValidationException("Expense details are required.");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new ValidationException("Expense category is required.");
        }
    }
}
