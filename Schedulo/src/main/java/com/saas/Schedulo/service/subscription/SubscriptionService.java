package com.saas.Schedulo.service.subscription;

import com.saas.Schedulo.dto.request.subscription.UpgradeSubscriptionRequest;
import com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;
import com.saas.Schedulo.dto.response.subscription.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    List<SubscriptionPlanResponse> getAvailablePlans();
    SubscriptionResponse getCurrentSubscription(UUID organizationId);
    SubscriptionResponse upgradeSubscription(UUID organizationId, UpgradeSubscriptionRequest request);
}
