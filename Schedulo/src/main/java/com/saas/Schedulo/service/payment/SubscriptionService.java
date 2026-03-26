package com.saas.Schedulo.service.payment;

import com.saas.Schedulo.dto.request.payment.CreateSubscriptionRequest;
import com.saas.Schedulo.dto.response.payment.SubscriptionResponse;

import java.util.UUID;

import java.util.List;
import com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request, UUID organizationId);
    SubscriptionResponse getByOrganization(UUID organizationId);
    SubscriptionResponse changePlan(UUID organizationId, UUID newPlanId);
    SubscriptionResponse changeBillingCycle(UUID organizationId, String billingCycle);
    void cancelSubscription(UUID organizationId, boolean immediate);
    void reactivateSubscription(UUID organizationId);
    List<SubscriptionPlanResponse> getAvailablePlans();
    SubscriptionPlanResponse getPlanById(UUID planId);
    boolean isFeatureEnabled(UUID organizationId, String featureKey);
    int getFeatureLimit(UUID organizationId, String featureKey);
}
