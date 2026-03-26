package com.saas.Schedulo.service.auth;

import com.saas.Schedulo.dto.request.auth.LoginRequest;
import com.saas.Schedulo.dto.request.auth.PasswordResetRequest;
import com.saas.Schedulo.dto.request.auth.RefreshTokenRequest;
import com.saas.Schedulo.dto.request.auth.SignupRequest;
import com.saas.Schedulo.dto.response.auth.AuthResponse;
import com.saas.Schedulo.dto.response.auth.TokenRefreshResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse signup(SignupRequest request);
    TokenRefreshResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    void initiatePasswordReset(String email);
    void resetPassword(PasswordResetRequest request);
    void verifyEmail(String token);
    AuthResponse processOAuth2Login(String provider, String code);
}
