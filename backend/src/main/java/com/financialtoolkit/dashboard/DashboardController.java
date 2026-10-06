package com.financialtoolkit.dashboard;

import com.financialtoolkit.common.ExpenseFilter;
import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Responses.*;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import com.financialtoolkit.recurring.RecurringRule;
import com.financialtoolkit.savings.SavingsEntry;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/dashboard")
public class DashboardController {
    private final ProfileService profileService;
    private final DashboardService dashboardService;

    public DashboardController(ProfileService profileService, DashboardService dashboardService) {
        this.profileService = profileService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        ExpenseFilter filter = new ExpenseFilter(search, category, startDate, endDate);
        DashboardSnapshot snapshot = dashboardService.buildSnapshot(profileId, filter);
        Profile profile = profileService.getProfile(profileId);
        List<RecurringRule> rules = profile.getRecurringRules().stream()
                .sorted(Comparator.comparing(RecurringRule::getDayOfMonth)).toList();
        List<SavingsEntry> savings = profile.getSavingsEntries().stream()
                .sorted(Comparator.comparing(SavingsEntry::getDate).reversed()).toList();
        List<InsightResponse> insightResponses = snapshot.getInsights().stream()
                .map(DtoMapper::insight).toList();
        List<InsightResponse> alertResponses = insightResponses.stream()
                .filter(item -> "warning".equals(item.type()) || "risk".equals(item.type()))
                .toList();
        return new DashboardResponse(
                profileId,
                DtoMapper.expenses(snapshot.getRecentExpenses()),
                rules.stream().map(DtoMapper::recurringRule).toList(),
                profile.getRecurringRules().stream().map(RecurringRule::getAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                snapshot.getUpcomingBills().stream().map(DtoMapper::bill).toList(),
                DtoMapper.budget(snapshot.getBudgetSummary()),
                DtoMapper.savingsGoal(profile.getGoal()),
                savings.stream().map(DtoMapper::savingsEntry).toList(),
                DtoMapper.savingsSummary(snapshot.getSavingsSummary()),
                DtoMapper.challenge(profile.getChallenge()),
                DtoMapper.challengeStats(snapshot.getChallengeStats()),
                snapshot.getCategoryTotals().stream().map(DtoMapper::categoryTotal).toList(),
                insightResponses,
                alertResponses
        );
    }
}
