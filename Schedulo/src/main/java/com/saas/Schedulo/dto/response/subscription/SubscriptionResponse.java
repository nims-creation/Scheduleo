package com.saas.Schedulo.dto.response.subscription;

import com.saas.Schedulo.entity.subscription.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID organizationId;
    private SubscriptionPlanResponse plan;
    private Subscription.SubscriptionStatus status;
    private Subscription.BillingCycle billingCycle;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private BigDecimal amount;
    private Boolean autoRenew;
}
