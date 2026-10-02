package org.skillbridge.security;

import org.skillbridge.entity.User;
import org.skillbridge.exception.NotAuthenticatedException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new NotAuthenticatedException("Not authenticated");
        }
        return userRepository.findById(cud.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}