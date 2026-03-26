package com.saas.Schedulo.repository.payment;

import com.saas.Schedulo.entity.subscription.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByCode(String code);
    List<SubscriptionPlan> findByIsActiveTrue();
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true " +
            "ORDER BY sp.sortOrder, sp.monthlyPrice")
    List<SubscriptionPlan> findAllActiveOrdered();
    @Query("SELECT sp FROM SubscriptionPlan sp LEFT JOIN FETCH sp.features " +
            "WHERE sp.id = :id")
    Optional<SubscriptionPlan> findByIdWithFeatures(@Param("id") UUID id);
}
