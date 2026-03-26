package com.saas.Schedulo.repository.payment;

import com.saas.Schedulo.entity.payment.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByOrganizationIdOrderByIssueDateDesc(UUID organizationId, Pageable pageable);
    @Query("SELECT i FROM Invoice i WHERE i.organization.id = :orgId " +
            "AND i.status = :status ORDER BY i.issueDate DESC")
    List<Invoice> findByOrganizationAndStatus(
            @Param("orgId") UUID organizationId,
            @Param("status") Invoice.InvoiceStatus status
    );
    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' " +
            "AND i.dueDate < :now AND i.isDeleted = false")
    List<Invoice> findOverdueInvoices(@Param("now") java.time.LocalDate now);
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 5) AS integer)), 0) " +
            "FROM Invoice i WHERE i.invoiceNumber LIKE :prefix%")
    int findMaxInvoiceNumber(@Param("prefix") String prefix);
}
