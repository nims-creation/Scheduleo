package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.organization.CreateDepartmentRequest;
import com.saas.Schedulo.dto.request.organization.UpdateDepartmentRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.organization.DepartmentResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.organization.DepartmentService;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "List departments", description = "List all top-level departments for the current organization")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> list(
            @CurrentUser CustomUserDetails currentUser) {
        List<DepartmentResponse> departments = departmentService.getByOrganization(currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Create a new department in the current organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = departmentService.create(currentUser.getOrganizationId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Department created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdateDepartmentRequest request) {
        DepartmentResponse response = departmentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Department updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Permanently delete a department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Department deleted successfully"));
    }
}
