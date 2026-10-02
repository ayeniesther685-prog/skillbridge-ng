package org.skillbridge.service;

import org.skillbridge.dto.request.LoginRequest;
import org.skillbridge.dto.request.RefreshRequest;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.dto.response.AuthResponse;
import org.skillbridge.dto.response.MeResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(RefreshRequest request);

    MeResponse me();
}