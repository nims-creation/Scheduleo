package com.saas.Schedulo.entity.subscription;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(name = "feature_name", nullable = false, length = 255)
    private String featureName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "limit_value")
    private Integer limitValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}

