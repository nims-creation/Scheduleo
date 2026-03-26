package com.saas.Schedulo.repository.payment;

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
