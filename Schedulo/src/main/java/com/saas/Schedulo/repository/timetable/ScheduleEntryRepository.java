package com.saas.Schedulo.repository.timetable;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {
    @Query("SELECT se FROM ScheduleEntry se WHERE se.timetable.id = :timetableId " +
            "AND se.isDeleted = false ORDER BY se.dayOfWeek, se.timeSlot.startTime")
    List<ScheduleEntry> findByTimetableId(@Param("timetableId") UUID timetableId);
    @Query("SELECT se FROM ScheduleEntry se WHERE se.timetable.id = :timetableId " +
            "AND se.dayOfWeek = :dayOfWeek AND se.isDeleted = false " +
            "ORDER BY se.timeSlot.startTime")
    List<ScheduleEntry> findByTimetableAndDay(
            @Param("timetableId") UUID timetableId,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek
    );
    @Query("SELECT se FROM ScheduleEntry se WHERE se.timetable.id = :timetableId " +
            "AND se.scheduleDate = :date AND se.isDeleted = false " +
            "ORDER BY se.startDatetime")
    List<ScheduleEntry> findByTimetableAndDate(
            @Param("timetableId") UUID timetableId,
            @Param("date") LocalDate date
    );
    @Query("SELECT se FROM ScheduleEntry se WHERE se.resource.id = :resourceId " +
            "AND se.scheduleDate = :date AND se.isDeleted = false " +
            "ORDER BY se.startDatetime")
    List<ScheduleEntry> findByResourceAndDate(
            @Param("resourceId") UUID resourceId,
            @Param("date") LocalDate date
    );
    @Query("SELECT se FROM ScheduleEntry se WHERE se.assignedTo.id = :userId " +
            "AND se.scheduleDate BETWEEN :startDate AND :endDate " +
            "AND se.isDeleted = false ORDER BY se.scheduleDate, se.startDatetime")
    List<ScheduleEntry> findByAssignedUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    // Conflict detection queries
    @Query("SELECT se FROM ScheduleEntry se WHERE se.resource.id = :resourceId " +
            "AND se.scheduleDate = :date " +
            "AND ((se.startDatetime < :endTime AND se.endDatetime > :startTime)) " +
            "AND se.id != :excludeId " +
            "AND se.isDeleted = false")
    List<ScheduleEntry> findConflictingByResource(
            @Param("resourceId") UUID resourceId,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime,
            @Param("excludeId") UUID excludeId
    );
    @Query("SELECT se FROM ScheduleEntry se WHERE se.assignedTo.id = :userId " +
            "AND se.scheduleDate = :date " +
            "AND ((se.startDatetime < :endTime AND se.endDatetime > :startTime)) " +
            "AND se.id != :excludeId " +
            "AND se.isDeleted = false")
    List<ScheduleEntry> findConflictingByUser(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime,
            @Param("excludeId") UUID excludeId
    );
    @Query("SELECT se FROM ScheduleEntry se JOIN se.participants p " +
            "WHERE p.id = :userId " +
            "AND se.scheduleDate BETWEEN :startDate AND :endDate " +
            "AND se.isDeleted = false")
    List<ScheduleEntry> findByParticipantAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT COUNT(se) FROM ScheduleEntry se " +
            "WHERE se.timetable.organization.id = :orgId " +
            "AND se.scheduleDate = :date " +
            "AND se.isDeleted = false")
    long countByOrganizationAndDate(
            @Param("orgId") UUID organizationId,
            @Param("date") LocalDate date
    );
}
