package com.financialtoolkit.profile;

import com.financialtoolkit.common.DuplicateResourceException;
import com.financialtoolkit.common.ResourceNotFoundException;
import com.financialtoolkit.common.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile createProfile(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Profile name is required.");
        }
        String normalizedName = name.trim();
        if (profileRepository.existsById(normalizedName)) {
            throw new DuplicateResourceException("Profile already exists.");
        }
        return profileRepository.save(new Profile(normalizedName));
    }

    public List<Profile> getProfiles() {
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparing(Profile::getId))
                .toList();
    }

    public Profile getProfile(String profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));
    }

    public void removeProfile(String profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found.");
        }
        profileRepository.deleteById(profileId);
    }

    public Profile refreshMonth(String profileId) {
        Profile profile = getProfile(profileId);
        synchronized (profile) {
            profile.getExpenses().clear();
            profile.getRecurringRules().forEach(rule -> rule.getAppliedPeriods().clear());
        }
        return profile;
    }
}
