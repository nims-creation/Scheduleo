package com.saas.Schedulo.repository.payment;

import com.saas.Schedulo.entity.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findByOrganizationIdAndIsDeletedFalse(UUID organizationId);
    Optional<PaymentMethod> findByOrganizationIdAndIsDefaultTrue(UUID organizationId);
    Optional<PaymentMethod> findByExternalMethodId(String externalMethodId);
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false " +
            "WHERE pm.organization.id = :orgId AND pm.id != :methodId")
    void clearDefaultForOrganization(
            @Param("orgId") UUID organizationId,
            @Param("methodId") UUID methodId
    );
}
