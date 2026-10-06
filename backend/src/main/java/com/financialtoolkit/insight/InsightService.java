package com.financialtoolkit.insight;

import com.financialtoolkit.profile.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InsightService {
    private static final int MAX_INSIGHTS = 6;
    private final List<InsightGenerator> generators;

    public InsightService(List<InsightGenerator> generators) {
        this.generators = generators;
    }

    public List<Insight> generateInsights(Profile profile, LocalDate today) {
        if (profile.getExpenses().isEmpty()) {
            return List.of(new Insight("info", "Add a few expenses to unlock trend insights."));
        }

        List<Insight> insights = new ArrayList<>();
        for (InsightGenerator generator : generators) {
            insights.addAll(generator.generate(profile, today));
        }

        if (insights.isEmpty()) {
            insights.add(new Insight("info", "No critical alerts right now."));
        }
        return insights.stream().limit(MAX_INSIGHTS).toList();
    }
}
