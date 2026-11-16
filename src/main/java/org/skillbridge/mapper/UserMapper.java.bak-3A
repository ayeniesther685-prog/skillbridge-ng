package org.skillbridge.mapper;

import org.skillbridge.dto.response.UserResponse;
import org.skillbridge.entity.Role;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getProfilePicture(),
                user.getBio(),
                user.getLocation(),
                user.isActive(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}