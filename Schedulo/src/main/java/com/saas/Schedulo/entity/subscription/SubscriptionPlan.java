package com.saas.Schedulo.entity.subscription;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "quarterly_price", precision = 10, scale = 2)
    private BigDecimal quarterlyPrice;

    @Column(name = "yearly_price", precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_schedules_per_day")
    private Integer maxSchedulesPerDay;

    @Column(name = "max_resources")
    private Integer maxResources;

    @Column(name = "max_departments")
    private Integer maxDepartments;

    @Column(name = "trial_days")
    @Builder.Default
    private Integer trialDays = 14;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanFeature> features = new ArrayList<>();

    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "stripe_price_id_monthly")
    private String stripePriceIdMonthly;

    @Column(name = "stripe_price_id_quarterly")
    private String stripePriceIdQuarterly;

    @Column(name = "stripe_price_id_yearly")
    private String stripePriceIdYearly;

    public enum PlanType {
        FREE, BASIC, PROFESSIONAL, ENTERPRISE, CUSTOM
    }

    public BigDecimal getPriceForCycle(Subscription.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> monthlyPrice;
            case QUARTERLY -> quarterlyPrice != null ? quarterlyPrice :
                    monthlyPrice.multiply(BigDecimal.valueOf(3));
            case YEARLY -> yearlyPrice != null ? yearlyPrice :
                    monthlyPrice.multiply(BigDecimal.valueOf(12));
        };
    }
}

