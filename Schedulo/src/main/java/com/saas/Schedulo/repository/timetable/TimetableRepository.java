package com.saas.Schedulo.repository.timetable;

import com.saas.Schedulo.entity.timetable.Timetable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, UUID> {
    @Query("SELECT t FROM Timetable t WHERE t.organization.id = :orgId " +
            "AND t.isDeleted = false ORDER BY t.effectiveFrom DESC")
    Page<Timetable> findByOrganizationId(@Param("orgId") UUID organizationId, Pageable pageable);
    @Query("SELECT t FROM Timetable t WHERE t.organization.id = :orgId " +
            "AND t.status = :status AND t.isDeleted = false")
    List<Timetable> findByOrganizationAndStatus(
            @Param("orgId") UUID organizationId,
            @Param("status") Timetable.TimetableStatus status
    );
    @Query("SELECT t FROM Timetable t WHERE t.organization.id = :orgId " +
            "AND t.status = 'PUBLISHED' " +
            "AND t.effectiveFrom <= :date " +
            "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) " +
            "AND t.isDeleted = false")
    List<Timetable> findActiveByOrganizationAndDate(
            @Param("orgId") UUID organizationId,
            @Param("date") LocalDate date
    );
    @Query("SELECT t FROM Timetable t WHERE t.organization.id = :orgId " +
            "AND t.department.id = :deptId " +
            "AND t.isDeleted = false")
    Page<Timetable> findByOrganizationAndDepartment(
            @Param("orgId") UUID organizationId,
            @Param("deptId") UUID departmentId,
            Pageable pageable
    );
    @Query("SELECT t FROM Timetable t WHERE t.isTemplate = true " +
            "AND (t.organization.id = :orgId OR t.organization IS NULL) " +
            "AND t.isDeleted = false")
    List<Timetable> findTemplates(@Param("orgId") UUID organizationId);
    @Query("SELECT t FROM Timetable t LEFT JOIN FETCH t.timeSlots " +
            "LEFT JOIN FETCH t.scheduleEntries WHERE t.id = :id AND t.isDeleted = false")
    Optional<Timetable> findByIdWithDetails(@Param("id") UUID id);
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Timetable t WHERE t.organization.id = :orgId " +
            "AND t.status = 'PUBLISHED' " +
            "AND t.effectiveFrom <= :endDate " +
            "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :startDate) " +
            "AND t.id != :excludeId " +
            "AND t.isDeleted = false")
    boolean existsOverlappingPublished(
            @Param("orgId") UUID organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId
    );
}
