package com.financialtoolkit.recurring;

import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.RecurringRuleApiRequest;
import com.financialtoolkit.common.api.Responses.RecurringRuleResponse;
import com.financialtoolkit.expense.PaymentMethod;
import com.financialtoolkit.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/recurring-rules")
public class RecurringRuleController {
    private final ProfileService profileService;
    private final RecurringExpenseService recurringExpenseService;

    public RecurringRuleController(ProfileService profileService, RecurringExpenseService recurringExpenseService) {
        this.profileService = profileService;
        this.recurringExpenseService = recurringExpenseService;
    }

    @GetMapping
    public List<RecurringRuleResponse> getRules(@PathVariable String profileId) {
        return profileService.getProfile(profileId).getRecurringRules().stream()
                .sorted(Comparator.comparing(RecurringRule::getDayOfMonth))
                .map(DtoMapper::recurringRule)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringRuleResponse addRule(@PathVariable String profileId, @Valid @RequestBody RecurringRuleApiRequest request) {
        RecurringRuleRequest serviceRequest = new RecurringRuleRequest(
                request.name(),
                request.amount(),
                PaymentMethod.fromDisplayName(request.paymentMethod()),
                request.dayOfMonth()
        );
        return DtoMapper.recurringRule(recurringExpenseService.addRule(profileId, serviceRequest));
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable String profileId, @PathVariable String ruleId) {
        recurringExpenseService.deleteRule(profileId, ruleId);
    }
}
