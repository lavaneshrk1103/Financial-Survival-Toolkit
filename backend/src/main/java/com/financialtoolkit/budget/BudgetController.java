package com.financialtoolkit.budget;

import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.BudgetRequest;
import com.financialtoolkit.common.api.Responses.BudgetResponse;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/budget")
public class BudgetController {
    private final ProfileService profileService;
    private final BudgetService budgetService;

    public BudgetController(ProfileService profileService, BudgetService budgetService) {
        this.profileService = profileService;
        this.budgetService = budgetService;
    }

    @GetMapping
    public BudgetResponse getBudget(@PathVariable String profileId) {
        Profile profile = profileService.getProfile(profileId);
        return DtoMapper.budget(budgetService.calculateSummary(profile, LocalDate.now()));
    }

    @PutMapping
    public BudgetResponse setBudget(@PathVariable String profileId, @Valid @RequestBody BudgetRequest request) {
        Profile profile = budgetService.setBudget(profileId, request.budget());
        return DtoMapper.budget(budgetService.calculateSummary(profile, LocalDate.now()));
    }
}
