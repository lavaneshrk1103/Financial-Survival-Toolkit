let profiles = {};
const API_BASE = "http://localhost:8080/api";
let currentProfile = null;
let editingExpenseId = null;
let editingSavingsId = null;
let chart;

const presetCategories = ["Food", "Travel", "Shopping"];
const currencyFormatter = new Intl.NumberFormat("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
});
const humanDateFormatter = new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric"
});

const profileSelect = document.getElementById("profileSelect");
const amountInput = document.getElementById("amount");
const expenseAmountErrorEl = document.getElementById("expenseAmountError");
const expenseDateInput = document.getElementById("expenseDate");
const categorySelect = document.getElementById("category");
const customCategoryInput = document.getElementById("customCategory");
const customCategoryLabel = document.getElementById("customCategoryLabel");
const paymentMethodSelect = document.getElementById("paymentMethod");
const noteInput = document.getElementById("note");
const submitExpenseBtn = document.getElementById("expenseSubmitBtn");
const cancelEditBtn = document.getElementById("cancelEditBtn");
const budgetProgressFill = document.getElementById("budgetProgressFill");
const budgetProgressPercent = document.getElementById("budgetProgressPercent");
const budgetInsight = document.getElementById("budgetInsight");
const expenseListEl = document.getElementById("expenseList");
const searchInput = document.getElementById("searchInput");
const filterCategorySelect = document.getElementById("filterCategory");
const filterStartDateInput = document.getElementById("filterStartDate");
const filterEndDateInput = document.getElementById("filterEndDate");
const clearFiltersBtn = document.getElementById("clearFiltersBtn");
const recurringNameInput = document.getElementById("recurringName");
const recurringAmountInput = document.getElementById("recurringAmount");
const recurringPaymentMethodSelect = document.getElementById("recurringPaymentMethod");
const recurringDayInput = document.getElementById("recurringDay");
const recurringListEl = document.getElementById("recurringList");
const recurringMonthlyTotalEl = document.getElementById("recurringMonthlyTotal");
const billCalendarListEl = document.getElementById("billCalendarList");
const insightsListEl = document.getElementById("insightsList");
const challengeDaysInput = document.getElementById("challengeDays");
const challengeStatusEl = document.getElementById("challengeStatus");
const challengeProgressEl = document.getElementById("challengeProgress");
const challengeStreakEl = document.getElementById("challengeStreak");
const goalInfoEl = document.getElementById("goalInfo");
const goalStatusBannerEl = document.getElementById("goalStatusBanner");
const goalTargetDisplayEl = document.getElementById("goalTargetDisplay");
const goalRemainingDisplayEl = document.getElementById("goalRemainingDisplay");
const goalMonthlySummaryEl = document.getElementById("goalMonthlySummary");
const savedAmountInput = document.getElementById("savedAmount");
const savedDateInput = document.getElementById("savedDate");
const savingsSubmitBtn = document.getElementById("savingsSubmitBtn");
const savingsCancelBtn = document.getElementById("savingsCancelBtn");
const savingsListEl = document.getElementById("savingsList");

function todayDate() {
    return new Date().toISOString().split("T")[0];
}

function parseDate(dateString) {
    return new Date(`${dateString}T00:00:00`);
}

function formatDateISO(date) {
    return date.toISOString().split("T")[0];
}

function formatDateHuman(dateString) {
    return humanDateFormatter.format(parseDate(dateString));
}

function addDays(date, days) {
    const next = new Date(date);
    next.setDate(next.getDate() + days);
    return next;
}

function daysBetween(startDate, endDate) {
    const msPerDay = 1000 * 60 * 60 * 24;
    const start = parseDate(formatDateISO(startDate)).getTime();
    const end = parseDate(formatDateISO(endDate)).getTime();
    return Math.floor((end - start) / msPerDay);
}

function generateExpenseId() {
    return `exp_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

function generateRecurringId() {
    return `rec_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    if (!response.ok) {
        let message = `Request failed (${response.status})`;
        try {
            const body = await response.json();
            message = body.message || (Array.isArray(body.messages) ? body.messages.join(", ") : message);
        } catch (_) { }
        throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
}

async function loadProfileFromApi(profileId) {
    const dashboard = await apiRequest(`/profiles/${encodeURIComponent(profileId)}/dashboard`);
    profiles[profileId] = {
        expenses: dashboard.recentExpenses || [],
        budget: Number(dashboard.budget?.budget || 0),
        recurringRules: dashboard.recurringRules || [],
        goal: dashboard.savingsGoal || { amount: 0, days: 0, startDate: "", dailyTarget: 0 },
        savingsEntries: dashboard.savingsEntries || [],
        challenge: dashboard.challenge || { active: false, startDate: "", endDate: "", targetDays: 0 },
        backendInsights: dashboard.insights || [],
        backendCategoryTotals: dashboard.categoryTotals || [],
        backendDashboard: dashboard
    };
    return profiles[profileId];
}

function saveProfiles() {
    // Data is now persisted by the Spring Boot backend.
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function monthKey(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function dueDateForMonth(rule, year, monthIndex) {
    const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();
    const dueDay = Math.min(Math.max(rule.dayOfMonth, 1), daysInMonth);
    return new Date(year, monthIndex, dueDay);
}

function ensureProfileShape(profile) {
    if (!Array.isArray(profile.expenses)) profile.expenses = [];
    if (typeof profile.budget !== "number") profile.budget = Number(profile.budget) || 0;
    if (!Array.isArray(profile.recurringRules)) profile.recurringRules = [];
    if (!profile.goal || typeof profile.goal !== "object") {
        profile.goal = {
            amount: 0,
            days: 0,
            startDate: "",
            dailyTarget: 0
        };
    }
    if (!Array.isArray(profile.savingsEntries)) profile.savingsEntries = [];
    if (!profile.challenge || typeof profile.challenge !== "object") {
        profile.challenge = {
            active: false,
            startDate: "",
            endDate: "",
            targetDays: 0
        };
    }
}

function normalizeProfiles() {
    let changed = false;

    for (const profileName in profiles) {
        const profile = profiles[profileName];
        ensureProfileShape(profile);

        const originalExpensesLength = profile.expenses.length;
        profile.expenses = profile.expenses
            .map((expense) => {
                const amount = Number(expense.amount);
                if (!amount || amount <= 0) return null;

                const normalized = {
                    id: expense.id || generateExpenseId(),
                    amount,
                    category: expense.category ? String(expense.category) : "Other",
                    date: expense.date || todayDate(),
                    note: expense.note ? String(expense.note) : "",
                    paymentMethod: expense.paymentMethod ? String(expense.paymentMethod) : "UPI",
                    recurringId: expense.recurringId ? String(expense.recurringId) : ""
                };

                if (
                    normalized.id !== expense.id ||
                    normalized.amount !== expense.amount ||
                    normalized.category !== expense.category ||
                    normalized.date !== expense.date ||
                    normalized.note !== expense.note ||
                    normalized.paymentMethod !== expense.paymentMethod ||
                    normalized.recurringId !== expense.recurringId
                ) {
                    changed = true;
                }
                return normalized;
            })
            .filter(Boolean);

        if (profile.expenses.length !== originalExpensesLength) changed = true;

        const originalRulesLength = profile.recurringRules.length;
        profile.recurringRules = profile.recurringRules
            .map((rule) => {
                const amount = Number(rule.amount);
                const dayOfMonth = Number(rule.dayOfMonth);
                if (!amount || amount <= 0 || !dayOfMonth) return null;

                const normalized = {
                    id: rule.id || generateRecurringId(),
                    name: rule.name ? String(rule.name) : "Recurring Expense",
                    amount,
                    category: rule.category ? String(rule.category) : "Other",
                    paymentMethod: rule.paymentMethod ? String(rule.paymentMethod) : "UPI",
                    dayOfMonth: Math.min(Math.max(dayOfMonth, 1), 31),
                    note: rule.note ? String(rule.note) : "",
                    startDate: rule.startDate || todayDate(),
                    appliedPeriods: Array.isArray(rule.appliedPeriods) ? rule.appliedPeriods : []
                };

                if (
                    normalized.id !== rule.id ||
                    normalized.name !== rule.name ||
                    normalized.amount !== rule.amount ||
                    normalized.category !== rule.category ||
                    normalized.paymentMethod !== rule.paymentMethod ||
                    normalized.dayOfMonth !== rule.dayOfMonth ||
                    normalized.note !== rule.note ||
                    normalized.startDate !== rule.startDate ||
                    normalized.appliedPeriods.length !== (rule.appliedPeriods || []).length
                ) {
                    changed = true;
                }
                return normalized;
            })
            .filter(Boolean);
        if (profile.recurringRules.length !== originalRulesLength) changed = true;

        const challenge = profile.challenge;
        const normalizedChallenge = {
            active: Boolean(challenge.active),
            startDate: challenge.startDate ? String(challenge.startDate) : "",
            endDate: challenge.endDate ? String(challenge.endDate) : "",
            targetDays: Number(challenge.targetDays) || 0
        };
        if (
            normalizedChallenge.active !== challenge.active ||
            normalizedChallenge.startDate !== challenge.startDate ||
            normalizedChallenge.endDate !== challenge.endDate ||
            normalizedChallenge.targetDays !== challenge.targetDays
        ) {
            profile.challenge = normalizedChallenge;
            changed = true;
        }

        const goal = profile.goal || {};
        const normalizedGoal = {
            amount: Number(goal.amount) || 0,
            days: Number(goal.days) || 0,
            startDate: goal.startDate ? String(goal.startDate) : "",
            dailyTarget: Number(goal.dailyTarget) || 0
        };
        if (
            normalizedGoal.amount !== goal.amount ||
            normalizedGoal.days !== goal.days ||
            normalizedGoal.startDate !== goal.startDate ||
            normalizedGoal.dailyTarget !== goal.dailyTarget
        ) {
            profile.goal = normalizedGoal;
            changed = true;
        }

        const originalSavingsLength = profile.savingsEntries.length;
        profile.savingsEntries = profile.savingsEntries
            .map((entry) => {
                const amount = Number(entry.amount);
                if (!amount || amount <= 0) return null;
                return {
                    id: entry.id || `sav_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
                    amount,
                    date: entry.date || todayDate()
                };
            })
            .filter(Boolean);
        if (profile.savingsEntries.length !== originalSavingsLength) changed = true;
    }

    if (changed) saveProfiles();
}

async function createProfile() {
    const name = document.getElementById("profileName").value.trim();
    if (!name || profiles[name]) return;
    try {
        await apiRequest("/profiles", { method: "POST", body: JSON.stringify({ name }) });
        document.getElementById("profileName").value = "";
        currentProfile = name;
        await loadProfiles();
    } catch (error) {
        alert(error.message);
    }
}

async function loadProfiles() {
    try {
        const list = await apiRequest("/profiles");
        profiles = {};
        for (const item of list) {
            profiles[item.id] = {
                expenses: [], budget: Number(item.budget || 0), recurringRules: [],
                goal: { amount: 0, days: 0, startDate: "", dailyTarget: 0 },
                savingsEntries: [], challenge: { active: false, startDate: "", endDate: "", targetDays: 0 }
            };
        }
        if (!currentProfile || !profiles[currentProfile]) {
            currentProfile = Object.keys(profiles)[0] || null;
        }
        profileSelect.innerHTML = "";
        for (const name of Object.keys(profiles)) {
            const option = document.createElement("option");
            option.value = name;
            option.textContent = name;
            profileSelect.appendChild(option);
        }
        profileSelect.value = currentProfile || "";
        if (currentProfile) await loadProfileFromApi(currentProfile);
        updateUI();
    } catch (error) {
        console.error(error);
        clearProfileViews();
        alert("Could not connect to the Java backend. Start Spring Boot on port 8080.");
    }
}

async function refreshCurrentProfileFromApi() {
    if (!currentProfile) return;
    await loadProfileFromApi(currentProfile);
    updateUI();
}

async function switchProfile() {
    currentProfile = profileSelect.value;
    cancelEdit();
    try {
        await loadProfileFromApi(currentProfile);
        updateUI();
    } catch (error) {
        alert(error.message);
    }
}

async function removeProfile() {
    if (!currentProfile) return;
    const name = currentProfile;
    const confirmed = window.confirm(`Remove profile "${name}"? This cannot be undone.`);
    if (!confirmed) return;
    try {
        await apiRequest(`/profiles/${encodeURIComponent(name)}`, { method: "DELETE" });
        currentProfile = null;
        await loadProfiles();
    } catch (error) {
        alert(error.message);
    }
}

async function refreshProfileMonth() {
    if (!currentProfile) return;
    const name = currentProfile;
    const confirmed = window.confirm(
        `Refresh "${name}" for a new month? This clears all expenses but keeps recurring rules, budget, and savings.`
    );
    if (!confirmed) return;
    try {
        await apiRequest(`/profiles/${encodeURIComponent(name)}/refresh-month`, { method: "POST" });
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

function getCategoryFromForm(categoryValue, customInput) {
    if (categoryValue !== "Other") return categoryValue;
    const customCategory = customInput.value.trim();
    if (!customCategory) return null;
    return customCategory;
}

function getExpensePayloadFromForm() {
    const amount = Number(amountInput.value);
    const category = getCategoryFromForm(categorySelect.value, customCategoryInput);
    const date = expenseDateInput.value || todayDate();
    const note = noteInput.value.trim();
    const paymentMethod = paymentMethodSelect.value;

    expenseAmountErrorEl.classList.add("hidden");
    expenseAmountErrorEl.textContent = "";

    if (!amount || amount <= 0) {
        expenseAmountErrorEl.textContent = "Amount must be greater than 0.";
        expenseAmountErrorEl.classList.remove("hidden");
        return null;
    }

    if (!category) return null;
    return { amount, category, date, note, paymentMethod };
}

function resetExpenseForm() {
    amountInput.value = "";
    expenseDateInput.value = todayDate();
    categorySelect.value = "Food";
    customCategoryInput.value = "";
    customCategoryInput.classList.add("hidden");
    customCategoryLabel.classList.add("hidden");
    paymentMethodSelect.value = "UPI";
    noteInput.value = "";
    expenseAmountErrorEl.classList.add("hidden");
    expenseAmountErrorEl.textContent = "";
}

function startEdit(expenseId) {
    if (!currentProfile) return;
    const profile = profiles[currentProfile];
    const expense = profile.expenses.find((item) => item.id === expenseId);
    if (!expense) return;

    editingExpenseId = expenseId;
    amountInput.value = String(expense.amount);
    expenseDateInput.value = expense.date || todayDate();
    noteInput.value = expense.note || "";
    paymentMethodSelect.value = expense.paymentMethod || "UPI";

    if (presetCategories.includes(expense.category)) {
        categorySelect.value = expense.category;
        customCategoryInput.value = "";
    } else {
        categorySelect.value = "Other";
        customCategoryInput.value = expense.category;
    }

    toggleCustomCategoryInput();
    submitExpenseBtn.textContent = "Save Changes";
    cancelEditBtn.classList.remove("hidden");
}

function cancelEdit() {
    editingExpenseId = null;
    submitExpenseBtn.textContent = "Add Expense";
    cancelEditBtn.classList.add("hidden");
    resetExpenseForm();
}

async function addExpense() {
    if (!currentProfile) return;
    const payload = getExpensePayloadFromForm();
    if (!payload) return;
    try {
        if (editingExpenseId) {
            await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/expenses/${encodeURIComponent(editingExpenseId)}`, {
                method: "PUT", body: JSON.stringify(payload)
            });
        } else {
            await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/expenses`, {
                method: "POST", body: JSON.stringify(payload)
            });
        }
        await refreshCurrentProfileFromApi();
        cancelEdit();
    } catch (error) {
        alert(error.message);
    }
}

async function setBudget() {
    if (!currentProfile) return;
    const budget = parseFloat(document.getElementById("budgetInput").value);
    try {
        await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/budget`, {
            method: "PUT", body: JSON.stringify({ budget: Number.isFinite(budget) ? budget : 0 })
        });
        document.getElementById("budgetInput").value = "";
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

async function setGoal() {
    if (!currentProfile) return;
    const goalAmount = parseFloat(document.getElementById("goalAmount").value);
    const days = parseFloat(document.getElementById("goalDays").value);
    if (!goalAmount || !days || goalAmount <= 0 || days <= 0) {
        goalInfoEl.textContent = "Enter valid goal amount and days.";
        return;
    }
    try {
        const goal = await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/savings/goal`, {
            method: "PUT", body: JSON.stringify({ amount: goalAmount, days })
        });
        goalInfoEl.textContent = "You need to save ₹" + currencyFormatter.format(goal.dailyTarget) + " per day.";
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

function resetSavingsForm() {
    savedAmountInput.value = "";
    savedDateInput.value = todayDate();
}

function cancelSavingsEdit() {
    editingSavingsId = null;
    savingsSubmitBtn.textContent = "Add Saved Amount";
    savingsCancelBtn.classList.add("hidden");
    resetSavingsForm();
}

async function addSavingsEntry() {
    if (!currentProfile) return;
    const amount = Number(savedAmountInput.value);
    const date = savedDateInput.value || todayDate();
    if (!amount || amount <= 0) return;
    try {
        if (editingSavingsId) {
            await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/savings?date=${encodeURIComponent(editingSavingsId)}`, { method: "DELETE" });
        }
        await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/savings`, {
            method: "POST", body: JSON.stringify({ amount, date })
        });
        cancelSavingsEdit();
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

async function addRecurringExpense() {
    if (!currentProfile) return;
    const name = recurringNameInput.value.trim();
    const amount = Number(recurringAmountInput.value);
    const paymentMethod = recurringPaymentMethodSelect.value;
    const dayOfMonth = Number(recurringDayInput.value);
    if (!name || !amount || amount <= 0 || !dayOfMonth || dayOfMonth < 1 || dayOfMonth > 31) return;
    try {
        await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/recurring-rules`, {
            method: "POST", body: JSON.stringify({ name, amount, paymentMethod, dayOfMonth })
        });
        recurringNameInput.value = "";
        recurringAmountInput.value = "";
        recurringDayInput.value = "";
        recurringPaymentMethodSelect.value = "UPI";
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

function applyRecurringExpenses(profile) {
    return false;
}

function updateBalance(profile) {
    const expenseTotal = profile.expenses.reduce((sum, exp) => sum + exp.amount, 0);
    const currentMonth = monthKey(new Date());
    const recurringCommitted = (profile.recurringRules || []).reduce((sum, rule) => {
        if (rule.appliedPeriods && rule.appliedPeriods.includes(currentMonth)) return sum;
        return sum + rule.amount;
    }, 0);
    const total = expenseTotal + recurringCommitted;
    document.getElementById("balance").textContent = currencyFormatter.format(total);

    const warningEl = document.getElementById("budgetWarning");
    if (profile.budget > 0 && total > profile.budget) {
        warningEl.textContent = "Budget exceeded.";
        warningEl.style.color = "#ffe8ea";
    } else if (profile.budget > 0) {
        const remaining = profile.budget - total;
        warningEl.textContent = `Budget left: ₹${currencyFormatter.format(Math.max(remaining, 0))}`;
        warningEl.style.color = "#e7fff4";
    } else {
        warningEl.textContent = "No monthly budget set yet.";
        warningEl.style.color = "#f2f6ff";
    }

    updateBudgetProgress(total, profile.budget);
}

function updateBudgetProgress(total, budget) {
    if (!budget || budget <= 0) {
        budgetProgressFill.style.width = "0%";
        budgetProgressFill.classList.remove("over");
        budgetProgressPercent.textContent = "0% used";
        budgetInsight.textContent = "Set budget to see burn rate";
        return;
    }

    const usedPercent = (total / budget) * 100;
    budgetProgressFill.style.width = `${Math.min(usedPercent, 100)}%`;
    budgetProgressFill.classList.toggle("over", usedPercent > 100);
    budgetProgressPercent.textContent = `${usedPercent.toFixed(1)}% used`;

    const now = new Date();
    const day = now.getDate();
    const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
    const remainingDays = Math.max(daysInMonth - day, 0);
    const remainingBudget = budget - total;
    const projected = (total / Math.max(day, 1)) * daysInMonth;

    if (remainingBudget <= 0) {
        budgetInsight.textContent = `Projected spend: ₹${currencyFormatter.format(projected)}.`;
        return;
    }

    const dailyLimit = remainingDays > 0 ? remainingBudget / remainingDays : remainingBudget;
    budgetInsight.textContent =
        `Projected: ₹${currencyFormatter.format(projected)} | Daily safe spend: ₹${currencyFormatter.format(Math.max(dailyLimit, 0))}`;
}

function updateCategoryFilterOptions(expenses) {
    const selectedValue = filterCategorySelect.value || "All";
    const categories = [...new Set(expenses.map((expense) => expense.category).sort())];

    filterCategorySelect.innerHTML = '<option value="All">All categories</option>';
    categories.forEach((category) => {
        const option = document.createElement("option");
        option.value = category;
        option.textContent = category;
        filterCategorySelect.appendChild(option);
    });

    filterCategorySelect.value = categories.includes(selectedValue) ? selectedValue : "All";
}

function getFilteredExpenses(expenses) {
    const searchTerm = searchInput.value.trim().toLowerCase();
    const selectedCategory = filterCategorySelect.value;
    const startDate = filterStartDateInput.value;
    const endDate = filterEndDateInput.value;

    return expenses
        .filter((expense) => {
            if (selectedCategory !== "All" && expense.category !== selectedCategory) return false;
            if (startDate && expense.date < startDate) return false;
            if (endDate && expense.date > endDate) return false;

            if (!searchTerm) return true;
            const haystack = `${expense.category} ${expense.note} ${expense.paymentMethod}`.toLowerCase();
            return haystack.includes(searchTerm);
        })
        .sort((a, b) => b.date.localeCompare(a.date));
}

function renderExpenseList(expenses) {
    expenseListEl.innerHTML = "";

    if (!expenses || expenses.length === 0) {
        expenseListEl.innerHTML = '<li class="empty-state">No expenses match these filters.</li>';
        return;
    }

    expenses.forEach((expense) => {
        const noteText = expense.note ? ` | ${escapeHtml(expense.note)}` : "";
        const recurringTag = expense.recurringId ? " | Recurring" : "";
        const item = document.createElement("li");
        item.className = "expense-item";
        item.innerHTML = `
            <div class="expense-meta">
                <strong>₹${currencyFormatter.format(expense.amount)}</strong>
                <span class="expense-category">${escapeHtml(expense.category)}</span>
                <span class="expense-submeta">${escapeHtml(expense.date)} | ${escapeHtml(expense.paymentMethod)}${noteText}${recurringTag}</span>
            </div>
            <div class="expense-actions">
                <button type="button" class="edit-btn" data-id="${expense.id}">Edit</button>
                <button type="button" class="delete-btn" data-id="${expense.id}">Delete</button>
            </div>
        `;
        expenseListEl.appendChild(item);
    });
}

function nextBills(profile, horizonDays = 45) {
    const today = parseDate(todayDate());
    const horizon = addDays(today, horizonDays);
    const bills = [];

    (profile.recurringRules || []).forEach((rule) => {
        for (let offset = 0; offset < 3; offset += 1) {
            const monthDate = new Date(today.getFullYear(), today.getMonth() + offset, 1);
            const dueDate = dueDateForMonth(rule, monthDate.getFullYear(), monthDate.getMonth());
            if (dueDate < today || dueDate > horizon) continue;

            bills.push({
                id: `${rule.id}_${monthKey(monthDate)}`,
                name: rule.name,
                amount: rule.amount,
                category: rule.category,
                dueDate: formatDateISO(dueDate),
                daysLeft: daysBetween(today, dueDate)
            });
        }
    });

    bills.sort((a, b) => a.dueDate.localeCompare(b.dueDate));
    return bills;
}

function renderBillCalendar(profile) {
    const bills = nextBills(profile, 30);
    billCalendarListEl.innerHTML = "";

    if (bills.length === 0) {
        billCalendarListEl.innerHTML = '<li class="empty-state">No upcoming recurring bills in the next 30 days.</li>';
        return;
    }

    bills.forEach((bill) => {
        const urgency = bill.daysLeft === 0 ? "Due today" : `Due in ${bill.daysLeft} day(s)`;
        const item = document.createElement("li");
        item.className = "compact-item";
        item.innerHTML = `
            <div>
                <strong>${escapeHtml(bill.name)} - ₹${currencyFormatter.format(bill.amount)}</strong>
                <div class="compact-meta">${escapeHtml(bill.category)} | ${escapeHtml(formatDateHuman(bill.dueDate))} | ${urgency}</div>
            </div>
        `;
        billCalendarListEl.appendChild(item);
    });
}

function renderRecurringList(rules) {
    recurringListEl.innerHTML = "";

    if (!rules || rules.length === 0) {
        recurringListEl.innerHTML = '<li class="empty-state">No recurring rules yet.</li>';
        return;
    }

    rules
        .slice()
        .sort((a, b) => a.dayOfMonth - b.dayOfMonth)
        .forEach((rule) => {
            const item = document.createElement("li");
            item.className = "compact-item";
            item.innerHTML = `
                <div>
                    <strong>${escapeHtml(rule.name)} - ₹${currencyFormatter.format(rule.amount)}</strong>
                    <div class="compact-meta">${escapeHtml(rule.category)} | Day ${rule.dayOfMonth} | ${escapeHtml(rule.paymentMethod)}</div>
                </div>
                <button type="button" class="delete-btn mini-btn" data-recurring-id="${rule.id}">Delete</button>
            `;
            recurringListEl.appendChild(item);
        });
}

function calculateNoSpendStats(profile) {
    const expenseDateSet = new Set(profile.expenses.map((expense) => expense.date));
    const today = parseDate(todayDate());
    let streak = 0;
    let cursor = new Date(today);

    for (let i = 0; i < 365; i += 1) {
        const key = formatDateISO(cursor);
        if (expenseDateSet.has(key)) break;
        streak += 1;
        cursor = addDays(cursor, -1);
    }

    const challenge = profile.challenge || { active: false };
    if (!challenge.startDate || !challenge.endDate) {
        return {
            streak,
            active: false,
            status: "No active challenge.",
            progress: "Start a challenge to track no-spend days."
        };
    }

    const start = parseDate(challenge.startDate);
    const end = parseDate(challenge.endDate);
    const effectiveEnd = today < end ? today : end;
    const elapsedDays = effectiveEnd >= start ? daysBetween(start, effectiveEnd) + 1 : 0;

    let noSpendDays = 0;
    for (let i = 0; i < elapsedDays; i += 1) {
        const day = formatDateISO(addDays(start, i));
        if (!expenseDateSet.has(day)) noSpendDays += 1;
    }

    const totalTarget = challenge.targetDays || daysBetween(start, end) + 1;
    const percent = totalTarget > 0 ? (noSpendDays / totalTarget) * 100 : 0;
    const completed = today > end;

    let status = challenge.active ? `Active until ${formatDateHuman(challenge.endDate)}.` : "Challenge paused.";
    if (completed && challenge.active) status = `Completed on ${formatDateHuman(challenge.endDate)}.`;

    return {
        streak,
        active: challenge.active && !completed,
        status,
        progress: `${noSpendDays}/${totalTarget} no-spend days (${percent.toFixed(1)}%).`
    };
}

function renderChallenge(profile) {
    const stats = calculateNoSpendStats(profile);
    challengeStatusEl.textContent = stats.status;
    challengeProgressEl.textContent = stats.progress;
    challengeStreakEl.textContent = `Current no-spend streak: ${stats.streak} day(s).`;
}

async function startNoSpendChallenge() {
    if (!currentProfile) return;
    const duration = Number(challengeDaysInput.value);
    if (!duration || duration <= 0) return;
    try {
        await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/challenge`, {
            method: "POST", body: JSON.stringify({ days: duration })
        });
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

async function stopNoSpendChallenge() {
    if (!currentProfile) return;
    try {
        await apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/challenge/stop`, { method: "PATCH" });
        await refreshCurrentProfileFromApi();
    } catch (error) {
        alert(error.message);
    }
}

function generateInsights(profile) {
    const insights = [];
    const expenses = profile.expenses.slice();
    if (expenses.length === 0) {
        return [{ type: "info", text: "Add a few expenses to unlock trend insights." }];
    }

    const now = new Date();
    const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    const prevMonthDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const prevMonth = `${prevMonthDate.getFullYear()}-${String(prevMonthDate.getMonth() + 1).padStart(2, "0")}`;

    const currentMonthExpenses = expenses.filter((expense) => expense.date.startsWith(currentMonth));
    const prevMonthExpenses = expenses.filter((expense) => expense.date.startsWith(prevMonth));
    const currentMonthTotal = currentMonthExpenses.reduce((sum, exp) => sum + exp.amount, 0);
    const prevMonthTotal = prevMonthExpenses.reduce((sum, exp) => sum + exp.amount, 0);

    if (prevMonthTotal > 0) {
        const deltaPercent = ((currentMonthTotal - prevMonthTotal) / prevMonthTotal) * 100;
        if (deltaPercent >= 15) {
            insights.push({
                type: "warning",
                text: `Spending is up ${deltaPercent.toFixed(1)}% vs last month.`
            });
        } else if (deltaPercent <= -10) {
            insights.push({
                type: "info",
                text: `Good trend: spending is down ${Math.abs(deltaPercent).toFixed(1)}% vs last month.`
            });
        }
    }

    if (currentMonthExpenses.length > 0) {
        const categoryTotals = {};
        currentMonthExpenses.forEach((expense) => {
            categoryTotals[expense.category] = (categoryTotals[expense.category] || 0) + expense.amount;
        });
        const [topCategory, topValue] = Object.entries(categoryTotals)
            .sort((a, b) => b[1] - a[1])[0];
        const share = (topValue / currentMonthTotal) * 100;
        if (share >= 45) {
            insights.push({
                type: "warning",
                text: `${topCategory} is ${share.toFixed(1)}% of this month's spend.`
            });
        }
    }

    if (expenses.length >= 5) {
        const sorted = expenses.slice().sort((a, b) => b.date.localeCompare(a.date));
        const latest = sorted[0];
        const baseline = sorted.slice(1, 11);
        const avgBaseline = baseline.reduce((sum, exp) => sum + exp.amount, 0) / Math.max(baseline.length, 1);
        if (latest.amount > avgBaseline * 2.2 && latest.amount > 500) {
            insights.push({
                type: "risk",
                text: `Anomaly detected: latest expense (₹${currencyFormatter.format(latest.amount)}) is much higher than recent average.`
            });
        }
    }

    if (profile.budget > 0) {
        const day = now.getDate();
        const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
        const projected = (currentMonthTotal / Math.max(day, 1)) * daysInMonth;
        if (projected > profile.budget * 1.1) {
            insights.push({
                type: "risk",
                text: `At current pace, projected monthly spend is ₹${currencyFormatter.format(projected)}.`
            });
        }
    }

    const challenge = profile.challenge || { active: false };
    if (challenge.active && challenge.startDate && challenge.endDate) {
        const today = todayDate();
        const spentToday = expenses.some((expense) => expense.date === today);
        if (spentToday) {
            insights.push({
                type: "warning",
                text: "No-spend challenge warning: you logged spending today."
            });
        } else {
            insights.push({
                type: "info",
                text: "No-spend challenge on track for today."
            });
        }
    }

    if (insights.length === 0) {
        insights.push({ type: "info", text: "No critical alerts right now." });
    }

    return insights.slice(0, 6);
}

function renderInsights(profile) {
    const insights = profile.backendInsights && profile.backendInsights.length
        ? profile.backendInsights
        : generateInsights(profile);
    insightsListEl.innerHTML = "";

    insights.forEach((insight) => {
        const item = document.createElement("li");
        item.className = `compact-item insight-${insight.type}`;
        item.innerHTML = `<div>${escapeHtml(insight.text)}</div>`;
        insightsListEl.appendChild(item);
    });
}

function renderRecurringOutput(profile) {
    const rules = profile.recurringRules || [];
    const monthlyTotal = rules.reduce((sum, rule) => sum + rule.amount, 0);

    recurringMonthlyTotalEl.textContent = `Monthly recurring total: ₹${currencyFormatter.format(monthlyTotal)}`;
}

function renderSavingsList(entries) {
    savingsListEl.innerHTML = "";
    if (!entries || entries.length === 0) {
        savingsListEl.innerHTML = '<li class="empty-state">No saved amounts logged yet.</li>';
        return;
    }

    const dailyTotals = {};
    entries.forEach((entry) => {
        dailyTotals[entry.date] = (dailyTotals[entry.date] || 0) + entry.amount;
    });

    Object.entries(dailyTotals)
        .sort((a, b) => b[0].localeCompare(a[0]))
        .forEach(([date, amount]) => {
            const item = document.createElement("li");
            item.className = "compact-item";
            item.innerHTML = `
                <div>
                    <strong>₹${currencyFormatter.format(amount)}</strong>
                    <div class="compact-meta">${escapeHtml(formatDateHuman(date))}</div>
                </div>
                <div class="expense-actions">
                    <button type="button" class="edit-btn" data-savings-date="${date}">Edit</button>
                    <button type="button" class="delete-btn" data-savings-date="${date}">Delete</button>
                </div>
            `;
            savingsListEl.appendChild(item);
        });
}

function renderSavingsGoal(profile) {
    const goal = profile.goal || { amount: 0, days: 0, dailyTarget: 0, startDate: "" };
    const totalSaved = (profile.savingsEntries || []).reduce((sum, entry) => sum + entry.amount, 0);

    if (!goal.amount || !goal.days) {
        goalTargetDisplayEl.textContent = "Set a goal to see your daily target.";
        goalRemainingDisplayEl.textContent = "Remaining: ₹0.00";
        goalStatusBannerEl.classList.add("hidden");
        goalMonthlySummaryEl.textContent = "";
        renderSavingsList(profile.savingsEntries);
        return;
    }

    const remaining = Math.max(goal.amount - totalSaved, 0);
    goalTargetDisplayEl.textContent = `Daily target: ₹${currencyFormatter.format(goal.dailyTarget)}`;
    goalRemainingDisplayEl.textContent = `Remaining: ₹${currencyFormatter.format(remaining)} (Saved: ₹${currencyFormatter.format(totalSaved)})`;
    if (remaining === 0) {
        goalStatusBannerEl.textContent = "Goal complete! You've hit your savings target.";
        goalStatusBannerEl.classList.remove("hidden");
        goalStatusBannerEl.classList.remove("warning");
    } else {
        const progress = (totalSaved / goal.amount) * 100;
        goalStatusBannerEl.textContent = `Progress: ${progress.toFixed(1)}% of goal saved.`;
        goalStatusBannerEl.classList.remove("hidden");
        goalStatusBannerEl.classList.add("warning");
    }

    const currentMonth = monthKey(new Date());
    const monthTotal = (profile.savingsEntries || [])
        .filter((entry) => entry.date.startsWith(currentMonth))
        .reduce((sum, entry) => sum + entry.amount, 0);
    if (monthTotal >= goal.amount) {
        goalMonthlySummaryEl.textContent = `This month you saved ₹${currencyFormatter.format(monthTotal)}. Goal achieved.`;
    } else {
        const percent = (monthTotal / goal.amount) * 100;
        const gap = goal.amount - monthTotal;
        goalMonthlySummaryEl.textContent =
            `This month you saved ₹${currencyFormatter.format(monthTotal)} (${percent.toFixed(1)}%). ₹${currencyFormatter.format(gap)} to go.`;
    }
    renderSavingsList(profile.savingsEntries);
}

function clearProfileViews() {
    document.getElementById("balance").textContent = "0.00";
    document.getElementById("budgetWarning").textContent = "Create a profile to start tracking.";
    budgetProgressFill.style.width = "0%";
    budgetProgressPercent.textContent = "0% used";
    budgetInsight.textContent = "Set budget to see burn rate";
    renderExpenseList([]);
    updateCategoryFilterOptions([]);
    updateChart({ expenses: [] });
    renderRecurringList([]);
    recurringMonthlyTotalEl.textContent = "";
    renderBillCalendar({ recurringRules: [] });
    insightsListEl.innerHTML = '<li class="empty-state">Create a profile to generate insights.</li>';
    challengeStatusEl.textContent = "No active challenge.";
    challengeProgressEl.textContent = "Start a challenge to track no-spend days.";
    challengeStreakEl.textContent = "Current no-spend streak: 0 day(s).";
    goalInfoEl.textContent = "";
    goalStatusBannerEl.textContent = "";
    goalStatusBannerEl.classList.add("hidden");
    goalTargetDisplayEl.textContent = "";
    goalRemainingDisplayEl.textContent = "";
    goalMonthlySummaryEl.textContent = "";
    savingsListEl.innerHTML = '<li class="empty-state">Create a profile to track savings.</li>';
}

function updateUI() {
    if (!currentProfile) {
        clearProfileViews();
        return;
    }

    const profile = profiles[currentProfile];
    ensureProfileShape(profile);

    const recurringChanged = applyRecurringExpenses(profile);
    if (recurringChanged) saveProfiles();

    updateBalance(profile);
    updateCategoryFilterOptions(profile.expenses);
    const filteredExpenses = getFilteredExpenses(profile.expenses);
    renderExpenseList(filteredExpenses);
    updateChart({ expenses: filteredExpenses });
    renderRecurringList(profile.recurringRules);
    renderRecurringOutput(profile);
    renderBillCalendar(profile);
    renderInsights(profile);
    renderChallenge(profile);
    renderSavingsGoal(profile);
}

function toggleCustomCategoryInput() {
    const shouldShow = categorySelect.value === "Other";
    customCategoryInput.classList.toggle("hidden", !shouldShow);
    customCategoryLabel.classList.toggle("hidden", !shouldShow);
}

function toggleRecurringCustomCategoryInput() {
    return;
}

expenseListEl.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement) || !currentProfile) return;

    const expenseId = target.dataset.id;
    if (!expenseId) return;

    if (target.classList.contains("delete-btn")) {
        apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/expenses/${encodeURIComponent(expenseId)}`, { method: "DELETE" })
            .then(async () => {
                if (editingExpenseId === expenseId) cancelEdit();
                await refreshCurrentProfileFromApi();
            })
            .catch((error) => alert(error.message));
        return;
    }

    if (target.classList.contains("edit-btn")) {
        startEdit(expenseId);
    }
});

recurringListEl.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement) || !currentProfile) return;
    if (!target.classList.contains("delete-btn")) return;

    const recurringId = target.dataset.recurringId;
    if (!recurringId) return;

    apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/recurring-rules/${encodeURIComponent(recurringId)}`, { method: "DELETE" })
        .then(() => refreshCurrentProfileFromApi())
        .catch((error) => alert(error.message));
});

function clearFilters() {
    searchInput.value = "";
    filterCategorySelect.value = "All";
    filterStartDateInput.value = "";
    filterEndDateInput.value = "";
    updateUI();
}

function updateChart(profile) {
    const categoryTotals = {};
    profile.expenses.forEach((expense) => {
        categoryTotals[expense.category] =
            (categoryTotals[expense.category] || 0) + expense.amount;
    });

    const labels = Object.keys(categoryTotals);
    const data = Object.values(categoryTotals);
    const chartEl = document.getElementById("expenseChart");
    if (chart) chart.destroy();

    chart = new Chart(chartEl, {
        type: "pie",
        data: {
            labels,
            datasets: [{
                data,
                backgroundColor: ["#2b67f6", "#1f9d6b", "#e88922", "#c84a73", "#6e59d9"],
                borderWidth: 2,
                borderColor: "#ffffff"
            }]
        },
        options: {
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        usePointStyle: true,
                        boxWidth: 12
                    }
                }
            }
        }
    });
}

categorySelect.addEventListener("change", toggleCustomCategoryInput);
searchInput.addEventListener("input", updateUI);
filterCategorySelect.addEventListener("change", updateUI);
filterStartDateInput.addEventListener("change", updateUI);
filterEndDateInput.addEventListener("change", updateUI);
clearFiltersBtn.addEventListener("click", clearFilters);

expenseDateInput.value = todayDate();
savedDateInput.value = todayDate();
toggleCustomCategoryInput();
toggleRecurringCustomCategoryInput();
loadProfiles();

savingsListEl.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement) || !currentProfile) return;

    const profile = profiles[currentProfile];
    ensureProfileShape(profile);

    const savingsDate = target.dataset.savingsDate;
    if (!savingsDate) return;

    if (target.classList.contains("delete-btn")) {
        apiRequest(`/profiles/${encodeURIComponent(currentProfile)}/savings?date=${encodeURIComponent(savingsDate)}`, { method: "DELETE" })
            .then(async () => {
                if (editingSavingsId) cancelSavingsEdit();
                await refreshCurrentProfileFromApi();
            })
            .catch((error) => alert(error.message));
        return;
    }

    if (target.classList.contains("edit-btn")) {
        const totalForDate = profile.savingsEntries
            .filter((item) => item.date === savingsDate)
            .reduce((sum, item) => sum + item.amount, 0);
        editingSavingsId = savingsDate;
        savedAmountInput.value = String(totalForDate);
        savedDateInput.value = savingsDate || todayDate();
        savingsSubmitBtn.textContent = "Save Entry";
        savingsCancelBtn.classList.remove("hidden");
    }
});
