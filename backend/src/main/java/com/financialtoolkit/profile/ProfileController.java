package com.financialtoolkit.profile;

import com.financialtoolkit.common.api.DtoMapper;
import com.financialtoolkit.common.api.Requests.CreateProfileRequest;
import com.financialtoolkit.common.api.Responses.ProfileResponse;
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

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<ProfileResponse> getProfiles() {
        return profileService.getProfiles().stream()
                .map(DtoMapper::profile)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return DtoMapper.profile(profileService.createProfile(request.name()));
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable String profileId) {
        profileService.removeProfile(profileId);
    }

    @PostMapping("/{profileId}/refresh-month")
    public ProfileResponse refreshMonth(@PathVariable String profileId) {
        return DtoMapper.profile(profileService.refreshMonth(profileId));
    }
}
