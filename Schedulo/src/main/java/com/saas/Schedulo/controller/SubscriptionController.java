package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.subscription.UpgradeSubscriptionRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;
import com.saas.Schedulo.dto.response.subscription.SubscriptionResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Endpoints for managing organization billing and subscriptions")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    @Operation(summary = "Get all available subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAvailablePlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAvailablePlans()));
    }

    @GetMapping("/current")
    @Operation(summary = "Get the current organization's subscription details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @CurrentUser CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getCurrentSubscription(currentUser.getOrganizationId())));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade organization subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradeSubscription(
            @Valid @RequestBody UpgradeSubscriptionRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        SubscriptionResponse response = subscriptionService.upgradeSubscription(currentUser.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription upgraded successfully"));
    }
}
