package com.saas.Schedulo.repository.payment;

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
