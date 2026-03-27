package com.saas.Schedulo.dto.request.subscription;

import com.saas.Schedulo.entity.subscription.Subscription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeSubscriptionRequest {
    @NotBlank(message = "Plan code is required")
    private String planCode;

    @NotNull(message = "Billing cycle is required")
    private Subscription.BillingCycle billingCycle;
}
