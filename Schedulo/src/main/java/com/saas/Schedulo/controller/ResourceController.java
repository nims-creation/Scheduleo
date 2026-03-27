package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.organization.CreateResourceRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.organization.ResourceResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.organization.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resource Management", description = "Endpoints for managing organization resources like rooms and equipment")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "Get all resources for the current organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAllResources(
            @CurrentUser CustomUserDetails currentUser) {
        List<ResourceResponse> resources = resourceService.getAllResources(currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific resource by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(
            @PathVariable UUID id,
            @CurrentUser CustomUserDetails currentUser) {
        ResourceResponse response = resourceService.getResourceById(id, currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody CreateResourceRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        ResourceResponse response = resourceService.createResource(request, currentUser.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resource created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable UUID id,
            @Valid @RequestBody CreateResourceRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        ResourceResponse response = resourceService.updateResource(id, request, currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(response, "Resource updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable UUID id,
            @CurrentUser CustomUserDetails currentUser) {
        resourceService.deleteResource(id, currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(null, "Resource deleted successfully"));
    }
}
