package com.saas.Schedulo.dto.request.user;

import jakarta.validation.constraints.*;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    private String phoneNumber;
    private String profileImageUrl;
    private String timezone;
    private String locale;
    
    private Set<String> roles;
    private Boolean isActive;
}
