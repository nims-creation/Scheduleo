package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.organization.UpdateOrganizationRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.organization.OrganizationResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.organization.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/me")
    @Operation(summary = "Get current organization", description = "Retrieve details of the organization the user belongs to")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getCurrentOrganization(
            @CurrentUser CustomUserDetails currentUser) {
        if (currentUser.getOrganizationId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You are not part of any organization.", null));
        }
        OrganizationResponse response = organizationService.getOrganizationById(currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(response, "Organization retrieved successfully"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current organization", description = "Update details and settings of current organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateCurrentOrganization(
            @CurrentUser CustomUserDetails currentUser,
            @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse response = organizationService.updateOrganization(currentUser.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated successfully"));
    }
}
