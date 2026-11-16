package org.skillbridge.service.impl;

import org.skillbridge.dto.request.LoginRequest;
import org.skillbridge.dto.request.RefreshRequest;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.dto.response.AuthResponse;
import org.skillbridge.dto.response.MeResponse;
import org.skillbridge.dto.response.UserResponse;
import org.skillbridge.entity.RefreshToken;
import org.skillbridge.entity.Role;
import org.skillbridge.entity.User;
import org.skillbridge.enums.RoleName;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.RefreshTokenRepository;
import org.skillbridge.repository.RoleRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CustomUserDetails;
import org.skillbridge.security.JwtService;
import org.skillbridge.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());

        Set<RoleName> requestedRoles = request.roles();

        if (requestedRoles == null || requestedRoles.isEmpty()) {
            requestedRoles = Set.of(RoleName.STUDENT);
        }

      for (RoleName roleName : requestedRoles) {
    if (roleName == RoleName.ADMIN) {
        throw new IllegalArgumentException("Cannot self-register as ADMIN");
    }
    Role role = roleRepository.findByName(roleName)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Role not found: " + roleName
                    )
            );
    user.addRole(role);
}

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        return createAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token")
                );

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        User user = refreshToken.getUser();

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "User account is inactive"
            );
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createAuthResponse(user);
    }

    @Override
    public void logout(RefreshRequest request) {

        refreshTokenRepository
                .findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponse me() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal()
                        instanceof CustomUserDetails userDetails)) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new MeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getProfilePicture(),
                user.getBio(),
                user.getLocation(),
                user.isActive(),
                roles
        );
    }

    private AuthResponse createAuthResponse(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken =
                jwtService.generateAccessToken(
                        user.getId(),
                        user.getEmail(),
                        roles
                );

        String refreshTokenValue =
                jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                Instant.now().plusSeconds(
                        jwtService.getRefreshTokenTtlSeconds()
                )
        );

        refreshTokenRepository.save(refreshToken);

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        UserResponse userResponse = new UserResponse(
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

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                "Bearer",
                jwtService.getAccessTokenTtlSeconds(),
                userResponse
        );
    }
}