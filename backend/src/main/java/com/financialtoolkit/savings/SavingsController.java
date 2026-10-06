package com.financialtoolkit.savings;

import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.SavingsEntryRequest;
import com.financialtoolkit.common.api.Requests.SavingsGoalRequest;
import com.financialtoolkit.common.api.Responses.SavingsEntryResponse;
import com.financialtoolkit.common.api.Responses.SavingsGoalResponse;
import com.financialtoolkit.common.api.Responses.SavingsSummaryResponse;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/savings")
public class SavingsController {
    private final ProfileService profileService;
    private final SavingsService savingsService;

    public SavingsController(ProfileService profileService, SavingsService savingsService) {
        this.profileService = profileService;
        this.savingsService = savingsService;
    }

    @GetMapping("/goal")
    public SavingsGoalResponse getGoal(@PathVariable String profileId) {
        Profile profile = profileService.getProfile(profileId);
        return DtoMapper.savingsGoal(profile.getGoal());
    }

    @PutMapping("/goal")
    public SavingsGoalResponse setGoal(@PathVariable String profileId, @Valid @RequestBody SavingsGoalRequest request) {
        return DtoMapper.savingsGoal(savingsService.setGoal(profileId, request.amount(), request.days()));
    }

    @GetMapping
    public List<SavingsEntryResponse> getEntries(@PathVariable String profileId) {
        return profileService.getProfile(profileId).getSavingsEntries().stream()
                .map(DtoMapper::savingsEntry).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsEntryResponse addEntry(@PathVariable String profileId, @Valid @RequestBody SavingsEntryRequest request) {
        return DtoMapper.savingsEntry(savingsService.addSavingsEntry(profileId, request.amount(), request.date()));
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable String profileId, @PathVariable String entryId) {
        savingsService.deleteSavingsEntry(profileId, entryId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntriesForDate(@PathVariable String profileId, @RequestParam LocalDate date) {
        savingsService.deleteSavingsForDate(profileId, date);
    }

    @GetMapping("/summary")
    public SavingsSummaryResponse getSummary(@PathVariable String profileId) {
        Profile profile = profileService.getProfile(profileId);
        return DtoMapper.savingsSummary(savingsService.calculateSummary(profile, LocalDate.now()));
    }
}
