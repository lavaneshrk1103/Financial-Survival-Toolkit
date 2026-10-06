package com.financialtoolkit.expense;

import com.financialtoolkit.common.ExpenseFilter;
import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.ExpenseApiRequest;
import com.financialtoolkit.common.api.Responses.ExpenseResponse;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/expenses")
public class ExpenseController {
    private final ProfileService profileService;
    private final ExpenseService expenseService;

    public ExpenseController(ProfileService profileService, ExpenseService expenseService) {
        this.profileService = profileService;
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(
            @PathVariable String profileId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Profile profile = profileService.getProfile(profileId);
        ExpenseFilter filter = new ExpenseFilter(search, category, startDate, endDate);
        return DtoMapper.expenses(expenseService.filterExpenses(profile, filter));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse addExpense(@PathVariable String profileId, @Valid @RequestBody ExpenseApiRequest request) {
        return DtoMapper.expense(expenseService.addExpense(profileId, toServiceRequest(request)));
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse updateExpense(@PathVariable String profileId, @PathVariable String expenseId,
                                         @Valid @RequestBody ExpenseApiRequest request) {
        return DtoMapper.expense(expenseService.updateExpense(profileId, expenseId, toServiceRequest(request)));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable String profileId, @PathVariable String expenseId) {
        expenseService.deleteExpense(profileId, expenseId);
    }

    private ExpenseRequest toServiceRequest(ExpenseApiRequest request) {
        return new ExpenseRequest(
                request.amount(),
                request.category(),
                request.date(),
                request.note(),
                PaymentMethod.fromDisplayName(request.paymentMethod())
        );
    }
}
