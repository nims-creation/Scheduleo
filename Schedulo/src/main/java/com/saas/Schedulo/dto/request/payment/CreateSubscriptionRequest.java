package com.saas.Schedulo.dto.request.payment;

import jakarta.validation.constraints.*;

import com.saas.Schedulo.entity.subscription.Subscription;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionRequest {
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    @NotNull(message = "Billing cycle is required")
    private Subscription.BillingCycle billingCycle;
    private String paymentMethodId;
    private String couponCode;
    private Boolean startTrial = true;
}
