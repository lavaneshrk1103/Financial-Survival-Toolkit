package com.financialtoolkit.recurring;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.IdGenerator;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.common.ResourceNotFoundException;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.expense.ExpenseService;
import com.financialtoolkit.expense.PaymentMethod;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class RecurringExpenseService {
    private final ProfileService profileService;
    private final ExpenseService expenseService;

    public RecurringExpenseService(ProfileService profileService, ExpenseService expenseService) {
        this.profileService = profileService;
        this.expenseService = expenseService;
    }

    public RecurringRule addRule(String profileId, RecurringRuleRequest request) {
        if (request == null) {
            throw new ValidationException("Recurring rule details are required.");
        }
        Profile profile = profileService.getProfile(profileId);
        RecurringRule rule = new RecurringRule(
                IdGenerator.recurringId(),
                request.getName(),
                request.getAmount(),
                request.getPaymentMethod() == null ? PaymentMethod.UPI : request.getPaymentMethod(),
                request.getDayOfMonth(),
                LocalDate.now()
        );
        synchronized (profile) {
            profile.getRecurringRules().add(rule);
        }
        return rule;
    }

    public void deleteRule(String profileId, String ruleId) {
        Profile profile = profileService.getProfile(profileId);
        synchronized (profile) {
            boolean removed = profile.getRecurringRules().removeIf(rule -> rule.getId().equals(ruleId));
            if (!removed) {
                throw new ResourceNotFoundException("Recurring rule not found.");
            }
        }
    }

    public boolean applyDueExpenses(Profile profile, LocalDate today) {
        boolean changed = false;
        synchronized (profile) {
            for (RecurringRule rule : profile.getRecurringRules()) {
                YearMonth iter = YearMonth.from(rule.getStartDate());
                YearMonth end = YearMonth.from(today);
                while (!iter.isAfter(end)) {
                    String period = "%d-%02d".formatted(iter.getYear(), iter.getMonthValue());
                    LocalDate dueDate = DateUtils.dueDateForMonth(rule.getDayOfMonth(), iter);
                    if (!rule.hasAppliedPeriod(period) && !dueDate.isAfter(today)) {
                        expenseService.addExpense(profile, rule, dueDate);
                        rule.markApplied(period);
                        changed = true;
                    }
                    iter = iter.plusMonths(1);
                }
            }
        }
        return changed;
    }

    public BigDecimal monthlyTotal(Profile profile) {
        return profile.getRecurringRules().stream()
                .map(RecurringRule::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
    }

    public List<BillCalendarItem> nextBills(Profile profile, LocalDate today, int horizonDays) {
        LocalDate horizon = today.plusDays(horizonDays);
        return profile.getRecurringRules().stream()
                .flatMap(rule -> List.of(0, 1, 2).stream().map(offset -> {
                    YearMonth month = YearMonth.from(today).plusMonths(offset);
                    LocalDate dueDate = DateUtils.dueDateForMonth(rule.getDayOfMonth(), month);
                    if (dueDate.isBefore(today) || dueDate.isAfter(horizon)) {
                        return null;
                    }
                    String id = rule.getId() + "_" + DateUtils.monthKey(dueDate);
                    long daysLeft = ChronoUnit.DAYS.between(today, dueDate);
                    return new BillCalendarItem(id, rule.getName(), rule.getAmount(), rule.getCategory(), dueDate, daysLeft);
                }))
                .filter(item -> item != null)
                .sorted(Comparator.comparing(BillCalendarItem::getDueDate))
                .toList();
    }
}
