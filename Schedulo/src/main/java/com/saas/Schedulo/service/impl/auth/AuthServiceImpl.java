package com.saas.Schedulo.service.impl.auth;



import com.saas.Schedulo.dto.mapper.UserMapper;
import com.saas.Schedulo.dto.request.auth.LoginRequest;
import com.saas.Schedulo.dto.request.auth.PasswordResetRequest;
import com.saas.Schedulo.dto.request.auth.RefreshTokenRequest;
import com.saas.Schedulo.dto.request.auth.SignupRequest;
import com.saas.Schedulo.dto.response.auth.AuthResponse;
import com.saas.Schedulo.dto.response.auth.TokenRefreshResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.organization.OrganizationType;
import com.saas.Schedulo.entity.user.RefreshToken;
import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.auth.AuthenticationException;
import com.saas.Schedulo.exception.auth.InvalidTokenException;
import com.saas.Schedulo.exception.auth.TokenExpiredException;
import com.saas.Schedulo.exception.resource.ResourceAlreadyExistsException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.user.RefreshTokenRepository;
import com.saas.Schedulo.repository.user.RoleRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.jwt.JwtTokenProvider;
import com.saas.Schedulo.service.auth.AuthService;
import com.saas.Schedulo.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final SlugGenerator slugGenerator;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Check lock status first (single lightweight query before the expensive bcrypt compare)
        User preCheck = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (preCheck.isAccountLocked()) {
            throw new AuthenticationException("Account is locked. Please try again later.");
        }

        try {
            // authenticate() internally calls loadUserByUsername → one DB round-trip + bcrypt compare
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Extract the already-loaded User from the principal — no extra DB call needed
            User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

            userRepository.resetLoginAttempts(user.getId());
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            RefreshToken refreshToken = createRefreshToken(user, request.getDeviceInfo());

            log.info("Login successful for user: {}", user.getEmail());

            return buildAuthResponse(user, accessToken, refreshToken.getToken());

        } catch (AuthenticationException e) {
            // Our own well-typed exceptions — re-throw as-is
            throw e;
        } catch (LockedException e) {
            throw new AuthenticationException("Account is locked. Please try again later.");
        } catch (DisabledException e) {
            throw new AuthenticationException("Account is disabled. Please contact support.");
        } catch (BadCredentialsException e) {
            // Increment failed-attempts only for genuinely wrong passwords
            userRepository.incrementFailedLoginAttempts(preCheck.getId());
            if (preCheck.getFailedLoginAttempts() >= 4) { // 4 past + this = 5
                preCheck.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(preCheck);
            }
            throw new AuthenticationException("Invalid email or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for {}: {}", request.getEmail(), e.getMessage(), e);
            throw new AuthenticationException("Login failed. Please try again.");
        }
    }

    @Override
    public AuthResponse signup(SignupRequest request) {
        log.info("Signup attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .authProvider(User.AuthProvider.LOCAL)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .roles(new java.util.HashSet<>(java.util.Set.of(userRole)))
                .build();

        user = userRepository.save(user);

        if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            Organization organization = createOrganization(request, user);
            user.setOrganization(organization);

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));
            user.getRoles().add(adminRole);
            
            user = userRepository.save(user);
        }

        log.info("User created successfully: {}", user.getEmail());

        // TODO: Send verification email

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user, null);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.getIsRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        // Rotate refresh token
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user, refreshToken.getDeviceInfo());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresAt(jwtTokenProvider.getExpirationFromToken(newAccessToken))
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void initiatePasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            // TODO: Send password reset email
        });
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Password reset");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUser(user.getId());
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    public AuthResponse processOAuth2Login(String provider, String code) {
        // Implementation depends on OAuth2 provider
        throw new UnsupportedOperationException("OAuth2 login not yet implemented");
    }

    private RefreshToken createRefreshToken(User user, String deviceInfo) {
        // Revoke existing tokens for this user (optional: or just this device)
        refreshTokenRepository.revokeAllByUser(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .deviceInfo(deviceInfo)
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private Organization createOrganization(SignupRequest request, User owner) {
        OrganizationType orgType = OrganizationType.OTHER;
        if (request.getOrganizationType() != null) {
            try {
                orgType = OrganizationType.valueOf(request.getOrganizationType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String baseSlug = slugGenerator.generateSlug(request.getOrganizationName());
        String finalSlug = baseSlug;
        int counter = 1;
        while (organizationRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        Organization organization = Organization.builder()
                .name(request.getOrganizationName())
                .slug(finalSlug)
                .organizationType(orgType)
                .owner(owner)
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .build();

        return organizationRepository.save(organization);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfileImageUrl())
                .timezone(user.getTimezone())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getResource() + ":" + p.getAction().name())
                        .distinct()
                        .toList())
                .build();

        if (user.getOrganization() != null) {
            Organization org = user.getOrganization();
            userInfo.setOrganization(AuthResponse.OrganizationInfo.builder()
                    .id(org.getId().toString())
                    .name(org.getName())
                    .slug(org.getSlug())
                    .type(org.getOrganizationType().name())
                    .logoUrl(org.getLogoUrl())
                    .build());
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresAt(jwtTokenProvider.getExpirationFromToken(accessToken))
                .user(userInfo)
                .build();
    }
}

