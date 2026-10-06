package com.financialtoolkit.insight;

import com.financialtoolkit.profile.Profile;

import java.time.LocalDate;
import java.util.List;

public interface InsightGenerator {
    List<Insight> generate(Profile profile, LocalDate today);
}
