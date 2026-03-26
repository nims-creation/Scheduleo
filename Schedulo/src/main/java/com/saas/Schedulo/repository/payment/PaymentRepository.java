package com.saas.Schedulo.repository.payment;

import com.saas.Schedulo.entity.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
