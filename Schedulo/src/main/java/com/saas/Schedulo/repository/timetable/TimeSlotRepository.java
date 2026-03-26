package com.saas.Schedulo.repository.timetable;

import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {
    List<TimeSlot> findByTimetableIdOrderBySortOrder(UUID timetableId);
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.timetable.id = :timetableId " +
            "AND ts.dayOfWeek = :dayOfWeek ORDER BY ts.startTime")
    List<TimeSlot> findByTimetableAndDay(
            @Param("timetableId") UUID timetableId,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek
    );
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.timetable.id = :timetableId " +
            "AND ts.slotType = :slotType ORDER BY ts.sortOrder")
    List<TimeSlot> findByTimetableAndType(
            @Param("timetableId") UUID timetableId,
            @Param("slotType") TimeSlot.SlotType slotType
    );
}
