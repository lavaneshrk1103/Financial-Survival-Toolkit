package com.financialtoolkit.challenge;

import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.ChallengeRequest;
import com.financialtoolkit.common.api.Responses.ChallengeDetailsResponse;
import com.financialtoolkit.profile.Profile;
import com.financialtoolkit.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles/{profileId}/challenge")
public class ChallengeController {
    private final ProfileService profileService;
    private final ChallengeService challengeService;

    public ChallengeController(ProfileService profileService, ChallengeService challengeService) {
        this.profileService = profileService;
        this.challengeService = challengeService;
    }

    @GetMapping
    public ChallengeDetailsResponse getChallenge(@PathVariable String profileId) {
        Profile profile = profileService.getProfile(profileId);
        return new ChallengeDetailsResponse(
                DtoMapper.challenge(profile.getChallenge()),
                DtoMapper.challengeStats(challengeService.calculateStats(profile, LocalDate.now()))
        );
    }

    @PostMapping
    public ChallengeDetailsResponse startChallenge(@PathVariable String profileId, @Valid @RequestBody ChallengeRequest request) {
        challengeService.startChallenge(profileId, request.days());
        Profile profile = profileService.getProfile(profileId);
        return new ChallengeDetailsResponse(
                DtoMapper.challenge(profile.getChallenge()),
                DtoMapper.challengeStats(challengeService.calculateStats(profile, LocalDate.now()))
        );
    }

    @PatchMapping("/stop")
    public ChallengeDetailsResponse stopChallenge(@PathVariable String profileId) {
        challengeService.stopChallenge(profileId);
        Profile profile = profileService.getProfile(profileId);
        return new ChallengeDetailsResponse(
                DtoMapper.challenge(profile.getChallenge()),
                DtoMapper.challengeStats(challengeService.calculateStats(profile, LocalDate.now()))
        );
    }
}
