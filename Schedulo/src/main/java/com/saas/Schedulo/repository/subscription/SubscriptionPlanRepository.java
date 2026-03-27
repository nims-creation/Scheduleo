package com.saas.Schedulo.repository.subscription;

import com.saas.Schedulo.entity.subscription.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByCode(String code);
    List<SubscriptionPlan> findAllByOrderBySortOrderAsc();
}
