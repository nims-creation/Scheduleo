package com.saas.Schedulo.repository.calendar;

import com.saas.Schedulo.entity.calendar.CalendarEvent;
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
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.organization.id = :orgId " +
            "AND ce.startDatetime >= :startDate AND ce.startDatetime <= :endDate " +
            "AND ce.isDeleted = false ORDER BY ce.startDatetime")
    List<CalendarEvent> findByOrganizationAndDateRange(
            @Param("orgId") UUID organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.creator.id = :userId " +
            "AND ce.startDatetime >= :startDate AND ce.startDatetime <= :endDate " +
            "AND ce.isDeleted = false ORDER BY ce.startDatetime")
    List<CalendarEvent> findByCreatorAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT ce FROM CalendarEvent ce JOIN ce.attendees a " +
            "WHERE a.id = :userId " +
            "AND ce.startDatetime >= :startDate AND ce.startDatetime <= :endDate " +
            "AND ce.isDeleted = false ORDER BY ce.startDatetime")
    List<CalendarEvent> findByAttendeeAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.organization.id = :orgId " +
            "AND ce.eventType = :eventType AND ce.isDeleted = false")
    Page<CalendarEvent> findByOrganizationAndType(
            @Param("orgId") UUID organizationId,
            @Param("eventType") CalendarEvent.EventType eventType,
            Pageable pageable
    );
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.isRecurring = true " +
            "AND ce.organization.id = :orgId AND ce.isDeleted = false")
    List<CalendarEvent> findRecurringEvents(@Param("orgId") UUID organizationId);
}
