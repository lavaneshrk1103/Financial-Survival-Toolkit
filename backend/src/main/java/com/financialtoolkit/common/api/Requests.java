package com.financialtoolkit.common.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
    private Requests() {
    }

    public record CreateProfileRequest(
            @NotBlank(message = "Profile name is required.") String name
    ) {
    }

    public record ExpenseApiRequest(
            @NotNull(message = "Amount is required.")
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
            BigDecimal amount,
            @NotBlank(message = "Category is required.") String category,
            LocalDate date,
            String note,
            String paymentMethod
    ) {
    }

    public record BudgetRequest(
            @NotNull(message = "Budget is required.") BigDecimal budget
    ) {
    }

    public record RecurringRuleApiRequest(
            @NotBlank(message = "Recurring name is required.") String name,
            @NotNull(message = "Amount is required.")
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
            BigDecimal amount,
            String paymentMethod,
            @Min(value = 1, message = "Due day must be between 1 and 31.") int dayOfMonth
    ) {
    }

    public record SavingsGoalRequest(
            @NotNull(message = "Goal amount is required.")
            @DecimalMin(value = "0.01", message = "Goal amount must be greater than 0.")
            BigDecimal amount,
            @Min(value = 1, message = "Days must be greater than 0.") int days
    ) {
    }

    public record SavingsEntryRequest(
            @NotNull(message = "Amount is required.")
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
            BigDecimal amount,
            LocalDate date
    ) {
    }

    public record ChallengeRequest(
            @Min(value = 1, message = "Challenge length must be greater than 0.") int days
    ) {
    }
}
