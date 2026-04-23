package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.organization.CreateBatchRequest;
import com.saas.Schedulo.dto.request.organization.UpdateBatchRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.organization.BatchResponse;
import com.saas.Schedulo.service.organization.BatchService;
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
@RequiredArgsConstructor
@Tag(name = "Batches", description = "Batch management endpoints — manage batches (sections/groups) within a department")
@SecurityRequirement(name = "bearerAuth")
public class BatchController {

    private final BatchService batchService;

    // ─── Nested under departments: GET /api/v1/departments/{deptId}/batches ───

    @GetMapping("/api/v1/departments/{deptId}/batches")
    @Operation(
            summary = "List batches for a department",
            description = "Returns all batches belonging to the given department, optionally filtered by semester"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> listByDepartment(
            @PathVariable UUID deptId,
            @RequestParam(required = false) Integer semester) {

        List<BatchResponse> batches = semester != null
                ? batchService.getByDepartmentAndSemester(deptId, semester)
                : batchService.getByDepartment(deptId);

        return ResponseEntity.ok(ApiResponse.success(batches));
    }

    // ─── Standalone: /api/v1/batches ─────────────────────────────────────────

    @PostMapping("/api/v1/batches")
    @Operation(
            summary = "Create a batch",
            description = "Create a new batch within a department (e.g. CSE-A for Semester 1)"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> create(
            @Valid @RequestBody CreateBatchRequest request) {
        BatchResponse response = batchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Batch created successfully"));
    }

    @GetMapping("/api/v1/batches/{id}")
    @Operation(summary = "Get batch by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BatchResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(batchService.getById(id)));
    }

    @PutMapping("/api/v1/batches/{id}")
    @Operation(summary = "Update batch", description = "Update batch details (name, semester, strength, etc.)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBatchRequest request) {
        BatchResponse response = batchService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Batch updated successfully"));
    }

    @DeleteMapping("/api/v1/batches/{id}")
    @Operation(summary = "Delete batch", description = "Permanently delete a batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        batchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted successfully"));
    }
}
