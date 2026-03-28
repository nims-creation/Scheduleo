package com.saas.Schedulo.repository.audit;

import com.saas.Schedulo.entity.base.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByOrganizationIdOrderByTimestampDesc(UUID organizationId, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.organizationId = :orgId " +
            "AND a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findByOrganizationAndAction(
            @Param("orgId") UUID organizationId,
            @Param("action") AuditLog.AuditAction action);

    @Query("SELECT a FROM AuditLog a WHERE a.organizationId = :orgId " +
            "AND a.entityType = :entityType AND a.entityId = :entityId " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntity(
            @Param("orgId") UUID organizationId,
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.organizationId = :orgId " +
            "AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditLog> findByOrganizationAndDateRange(
            @Param("orgId") UUID organizationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.organizationId = :orgId " +
            "AND a.timestamp >= :since")
    long countRecentByOrganization(
            @Param("orgId") UUID organizationId,
            @Param("since") LocalDateTime since);
}
