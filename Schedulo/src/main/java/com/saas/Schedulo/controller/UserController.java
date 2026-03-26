package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.user.CreateUserRequest;
import com.saas.Schedulo.dto.request.user.UpdateUserRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.user.UserResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user within an organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get detailed information about a user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isSelf(authentication, #id)")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get detailed information about the currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        UserResponse response = userService.getById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user attributes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isSelf(authentication, #id)")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "List users", description = "List all users for the current user's organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listUsers(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserResponse> response = userService.getByOrganization(currentUser.getOrganizationId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users in the organization by name or email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> searchUsers(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserResponse> response = userService.search(currentUser.getOrganizationId(), query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
