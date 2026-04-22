package com.saas.Schedulo.dto.response.auth;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Instant expiresAt;
    private UserInfo user;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String profileImageUrl;
        private String timezone;
        private List<String> roles;
        private List<String> permissions;
        private OrganizationInfo organization;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrganizationInfo {
        private String id;
        private String name;
        private String slug;
        private String type;
        private String logoUrl;
    }
}
