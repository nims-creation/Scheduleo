package com.saas.Schedulo.repository.timetable;

import com.saas.Schedulo.entity.timetable.TeacherAbsence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherAbsenceRepository extends JpaRepository<TeacherAbsence, UUID> {

    /**
     * Check if a teacher is already marked absent on a given date for the same org.
     */
    boolean existsByTeacherIdAndAbsentDateAndIsDeletedFalse(UUID teacherId, LocalDate absentDate);

    /**
     * Find a specific absence record by teacher + date (for idempotency / update flows).
     */
    Optional<TeacherAbsence> findByTeacherIdAndAbsentDateAndIsDeletedFalse(UUID teacherId, LocalDate absentDate);

    /**
     * All absences for an organization within a date range — used by admin dashboard.
     */
    @Query("""
            SELECT ta FROM TeacherAbsence ta
            WHERE ta.organization.id = :orgId
              AND ta.absentDate BETWEEN :from AND :to
              AND ta.isDeleted = false
            ORDER BY ta.absentDate DESC, ta.teacher.firstName ASC
            """)
    List<TeacherAbsence> findByOrganizationAndDateRange(
            @Param("orgId") UUID organizationId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * All absences for a specific teacher in a date range.
     */
    @Query("""
            SELECT ta FROM TeacherAbsence ta
            WHERE ta.teacher.id = :teacherId
              AND ta.absentDate BETWEEN :from AND :to
              AND ta.isDeleted = false
            ORDER BY ta.absentDate DESC
            """)
    List<TeacherAbsence> findByTeacherAndDateRange(
            @Param("teacherId") UUID teacherId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * Count absences per teacher for a month — useful for analytics/reports.
     */
    @Query("""
            SELECT COUNT(ta) FROM TeacherAbsence ta
            WHERE ta.teacher.id = :teacherId
              AND ta.absentDate BETWEEN :from AND :to
              AND ta.isDeleted = false
            """)
    long countAbsencesByTeacherInRange(
            @Param("teacherId") UUID teacherId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
