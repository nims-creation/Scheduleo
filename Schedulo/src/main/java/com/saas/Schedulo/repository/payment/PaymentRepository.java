package com.saas.Schedulo.repository.payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
    @Query("SELECT p FROM Payment p WHERE p.organization.id = :orgId " +
            "AND p.status = :status AND p.isDeleted = false")
    List<Payment> findByOrganizationAndStatus(
            @Param("orgId") UUID organizationId,
            @Param("status") Payment.PaymentStatus status
    );
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subId " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findBySubscriptionId(@Param("subId") UUID subscriptionId);
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.organization.id = :orgId " +
            "AND p.status = 'SUCCEEDED' AND p.paidAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumPaymentsByOrganizationAndPeriod(
            @Param("orgId") UUID organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
