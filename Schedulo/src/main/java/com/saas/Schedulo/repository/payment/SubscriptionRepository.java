package com.saas.Schedulo.repository.payment;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByOrganizationId(UUID organizationId);
    Optional<Subscription> findByExternalSubscriptionId(String externalId);
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.isDeleted = false")
    List<Subscription> findByStatus(@Param("status") Subscription.SubscriptionStatus status);
    @Query("SELECT s FROM Subscription s WHERE s.currentPeriodEnd <= :date " +
            "AND s.status = 'ACTIVE' AND s.isDeleted = false")
    List<Subscription> findExpiringBefore(@Param("date") LocalDateTime date);
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate <= :date " +
            "AND s.status = 'ACTIVE' AND s.autoRenew = true AND s.isDeleted = false")
    List<Subscription> findDueForRenewal(@Param("date") LocalDateTime date);
    @Modifying
    @Query("UPDATE Subscription s SET s.status = 'EXPIRED' " +
            "WHERE s.currentPeriodEnd < :now AND s.status = 'ACTIVE'")
    int expireOverdueSubscriptions(@Param("now") LocalDateTime now);
}
