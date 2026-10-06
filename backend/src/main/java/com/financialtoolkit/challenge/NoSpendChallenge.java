package com.financialtoolkit.challenge;

import java.time.LocalDate;

public class NoSpendChallenge {
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
    private int targetDays;

    public NoSpendChallenge() {
        this(false, null, null, 0);
    }

    public NoSpendChallenge(boolean active, LocalDate startDate, LocalDate endDate, int targetDays) {
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetDays = targetDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public boolean isConfigured() {
        return startDate != null && endDate != null && targetDays > 0;
    }
}
