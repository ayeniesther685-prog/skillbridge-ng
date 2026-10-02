package org.skillbridge.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.dto.response.AuthResponse;
import org.skillbridge.entity.Role;
import org.skillbridge.entity.User;
import org.skillbridge.enums.RoleName;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.RefreshTokenRepository;
import org.skillbridge.repository.RoleRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;

    @InjectMocks AuthServiceImpl authService;

    @Test
    void register_hashesPassword_andReturnsTokens() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hash");

        Role studentRole = new Role(RoleName.STUDENT);
        studentRole.setId((short) 1);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        when(jwtService.generateAccessToken(any(), anyString(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(jwtService.getRefreshTokenTtlSeconds()).thenReturn(86400L);

        RegisterRequest req = new RegisterRequest(
                "Ada", "Obi", "ada@example.com", "Password123", "+2348000000000",
                Set.of(RoleName.STUDENT));

        AuthResponse res = authService.register(req);

        assertThat(res.accessToken()).isEqualTo("access-token");
        assertThat(res.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("$2a$hash");
        assertThat(saved.getEmail()).isEqualTo("ada@example.com");
        assertThat(saved.getRoles()).hasSize(1);
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest(
                "Ada", "Obi", "ada@example.com", "Password123", null, null);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_unknownRole_throwsResourceNotFound() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(roleRepository.findByName(any(RoleName.class))).thenReturn(Optional.empty());

        RegisterRequest req = new RegisterRequest(
                "Ada", "Obi", "ada@example.com", "Password123", null,
                Set.of(RoleName.STUDENT));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");
    }
}