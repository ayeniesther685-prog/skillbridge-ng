package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ProfileRequest;
import org.skillbridge.dto.response.ProfileResponse;
import org.skillbridge.entity.Profile;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ProfileMapper;
import org.skillbridge.repository.ProfileRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileServiceImpl {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final CurrentUser currentUser;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              UserRepository userRepository,
                              ProfileMapper profileMapper,
                              CurrentUser currentUser) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
        this.currentUser = currentUser;
    }

    public ProfileResponse getForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return profileRepository.findByUserId(userId)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user " + userId));
    }

    public ProfileResponse getForCurrentUser() {
        User u = currentUser.require();
        return profileRepository.findByUserId(u.getId())
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not yet created"));
    }

    @Transactional
    public ProfileResponse upsertForCurrentUser(ProfileRequest req) {
        User u = currentUser.require();
        Profile p = profileRepository.findByUserId(u.getId()).orElseGet(() -> {
            Profile np = new Profile();
            np.setUser(u);
            return np;
        });
        p.setHeadline(req.headline());
        p.setWebsite(req.website());
        p.setLinkedinUrl(req.linkedinUrl());
        p.setGithubUrl(req.githubUrl());
        p.setTwitterUrl(req.twitterUrl());
        p.setAvailability(req.availability());
        p.setHourlyRate(req.hourlyRate());
        return profileMapper.toResponse(profileRepository.save(p));
    }
}