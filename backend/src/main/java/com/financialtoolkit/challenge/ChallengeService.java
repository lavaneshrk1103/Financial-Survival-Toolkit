package com.financialtoolkit.challenge;

import com.financialtoolkit.common.DateUtils;
import com.financialtoolkit.common.ValidationException;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ProfileService profileService;

    public ChallengeService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public NoSpendChallenge startChallenge(String profileId, int days) {
        if (days <= 0) {
            throw new ValidationException("Challenge length must be greater than 0.");
        }
        Profile profile = profileService.getProfile(profileId);
        LocalDate start = LocalDate.now();
        NoSpendChallenge challenge = new NoSpendChallenge(true, start, start.plusDays(days - 1L), days);
        profile.setChallenge(challenge);
        return challenge;
    }

    public NoSpendChallenge stopChallenge(String profileId) {
        Profile profile = profileService.getProfile(profileId);
        profile.getChallenge().setActive(false);
        return profile.getChallenge();
    }

    public ChallengeStats calculateStats(Profile profile, LocalDate today) {
        Set<LocalDate> expenseDates = profile.getExpenses().stream()
                .map(expense -> expense.getDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate cursor = today;
        for (int index = 0; index < 365; index += 1) {
            if (expenseDates.contains(cursor)) {
                break;
            }
            streak += 1;
            cursor = cursor.minusDays(1);
        }

        NoSpendChallenge challenge = profile.getChallenge();
        if (!challenge.isConfigured()) {
            return new ChallengeStats(streak, false, "No active challenge.", "Start a challenge to track no-spend days.");
        }

        LocalDate effectiveEnd = today.isBefore(challenge.getEndDate()) ? today : challenge.getEndDate();
        long elapsedDays = effectiveEnd.isBefore(challenge.getStartDate())
                ? 0
                : DateUtils.daysBetweenInclusive(challenge.getStartDate(), effectiveEnd);

        int noSpendDays = 0;
        for (int index = 0; index < elapsedDays; index += 1) {
            LocalDate day = challenge.getStartDate().plusDays(index);
            if (!expenseDates.contains(day)) {
                noSpendDays += 1;
            }
        }

        int targetDays = challenge.getTargetDays();
        double percent = targetDays > 0 ? (noSpendDays * 100.0) / targetDays : 0;
        boolean completed = today.isAfter(challenge.getEndDate());
        String status = challenge.isActive() ? "Active until " + challenge.getEndDate() + "." : "Challenge paused.";
        if (completed && challenge.isActive()) {
            status = "Completed on " + challenge.getEndDate() + ".";
        }

        return new ChallengeStats(
                streak,
                challenge.isActive() && !completed,
                status,
                "%d/%d no-spend days (%.1f%%).".formatted(noSpendDays, targetDays, percent)
        );
    }
}
