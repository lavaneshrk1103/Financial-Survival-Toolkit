package com.financialtoolkit.challenge;

public class ChallengeStats {
    private final int streak;
    private final boolean active;
    private final String status;
    private final String progress;

    public ChallengeStats(int streak, boolean active, String status, String progress) {
        this.streak = streak;
        this.active = active;
        this.status = status;
        this.progress = progress;
    }

    public int getStreak() {
        return streak;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatus() {
        return status;
    }

    public String getProgress() {
        return progress;
    }
}
