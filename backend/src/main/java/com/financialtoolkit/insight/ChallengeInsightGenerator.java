package com.financialtoolkit.insight;

import com.financialtoolkit.challenge.NoSpendChallenge;
import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ChallengeInsightGenerator implements InsightGenerator {
    @Override
    public List<Insight> generate(Profile profile, LocalDate today) {
        NoSpendChallenge challenge = profile.getChallenge();
        if (!challenge.isActive() || !challenge.isConfigured()) {
            return List.of();
        }
        boolean spentToday = profile.getExpenses().stream().anyMatch(expense -> expense.getDate().equals(today));
        if (spentToday) {
            return List.of(new Insight("warning", "No-spend challenge warning: you logged spending today."));
        }
        return List.of(new Insight("info", "No-spend challenge on track for today."));
    }
}
