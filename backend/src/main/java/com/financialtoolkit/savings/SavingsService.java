package com.financialtoolkit.savings;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.IdGenerator;
import com.financialtoolkit.common.MoneyUtils;
import com.financialtoolkit.common.ResourceNotFoundException;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SavingsService {
    private final ProfileService profileService;

    public SavingsService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public SavingsGoal setGoal(String profileId, BigDecimal amount, int days) {
        if (MoneyUtils.isNotPositive(amount) || days <= 0) {
            throw new ValidationException("Enter valid goal amount and days.");
        }
        Profile profile = profileService.getProfile(profileId);
        SavingsGoal goal = new SavingsGoal(amount, days, LocalDate.now());
        profile.setGoal(goal);
        return goal;
    }

    public SavingsEntry addSavingsEntry(String profileId, BigDecimal amount, LocalDate date) {
        return addSavingsEntry(profileService.getProfile(profileId), amount, date);
    }

    public SavingsEntry addSavingsEntry(Profile profile, BigDecimal amount, LocalDate date) {
        SavingsEntry entry = new SavingsEntry(IdGenerator.savingsId(), amount, date == null ? LocalDate.now() : date);
        synchronized (profile) {
            profile.getSavingsEntries().add(entry);
        }
        return entry;
    }

    public SavingsEntry replaceSavingsForDate(String profileId, LocalDate date, BigDecimal amount) {
        Profile profile = profileService.getProfile(profileId);
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        synchronized (profile) {
            profile.getSavingsEntries().removeIf(entry -> entry.getDate().equals(effectiveDate));
            return addSavingsEntry(profile, amount, effectiveDate);
        }
    }

    public void deleteSavingsForDate(String profileId, LocalDate date) {
        Profile profile = profileService.getProfile(profileId);
        synchronized (profile) {
            profile.getSavingsEntries().removeIf(entry -> entry.getDate().equals(date));
        }
    }

    public void deleteSavingsEntry(String profileId, String entryId) {
        Profile profile = profileService.getProfile(profileId);
        synchronized (profile) {
            boolean removed = profile.getSavingsEntries().removeIf(entry -> entry.getId().equals(entryId));
            if (!removed) {
                throw new ResourceNotFoundException("Savings entry not found.");
            }
        }
    }

    public SavingsSummary calculateSummary(Profile profile, LocalDate today) {
        BigDecimal totalSaved = profile.getSavingsEntries().stream()
                .map(SavingsEntry::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
        SavingsGoal goal = profile.getGoal();
        BigDecimal remaining = goal.isConfigured()
                ? MoneyUtils.normalize(goal.getAmount().subtract(totalSaved).max(BigDecimal.ZERO))
                : MoneyUtils.ZERO;
        String currentMonth = DateUtils.monthKey(today);
        BigDecimal currentMonthSaved = profile.getSavingsEntries().stream()
                .filter(entry -> DateUtils.monthKey(entry.getDate()).equals(currentMonth))
                .map(SavingsEntry::getAmount)
                .reduce(MoneyUtils.ZERO, MoneyUtils::add);
        return new SavingsSummary(totalSaved, remaining, currentMonthSaved, goal.isConfigured() && remaining.compareTo(BigDecimal.ZERO) == 0);
    }
}
