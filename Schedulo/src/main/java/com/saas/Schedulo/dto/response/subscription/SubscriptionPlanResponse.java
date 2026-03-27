package com.saas.Schedulo.dto.response.subscription;

import com.saas.Schedulo.entity.subscription.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private SubscriptionPlan.PlanType planType;
    private Integer maxUsers;
    private Integer maxSchedulesPerDay;
    private Boolean isPopular;
}
