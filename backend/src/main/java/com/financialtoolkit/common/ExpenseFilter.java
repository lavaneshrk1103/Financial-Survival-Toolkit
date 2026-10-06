package com.financialtoolkit.common;

import java.time.LocalDate;

public class ExpenseFilter {
    private String searchTerm = "";
    private String category = "All";
    private LocalDate startDate;
    private LocalDate endDate;

    public ExpenseFilter() {
    }

    public ExpenseFilter(String searchTerm, String category, LocalDate startDate, LocalDate endDate) {
        this.searchTerm = searchTerm == null ? "" : searchTerm;
        this.category = category == null || category.isBlank() ? "All" : category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
