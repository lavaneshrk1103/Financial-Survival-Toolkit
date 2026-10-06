package com.financialtoolkit.expense;

public enum PaymentMethod {
    UPI,
    CARD,
    CASH,
    BANK_TRANSFER;

    public static PaymentMethod fromDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return UPI;
        }
        return switch (value.trim().toLowerCase()) {
            case "card" -> CARD;
            case "cash" -> CASH;
            case "bank transfer" -> BANK_TRANSFER;
            default -> UPI;
        };
    }

    public String toDisplayName() {
        return switch (this) {
            case UPI -> "UPI";
            case CARD -> "Card";
            case CASH -> "Cash";
            case BANK_TRANSFER -> "Bank Transfer";
        };
    }
}
