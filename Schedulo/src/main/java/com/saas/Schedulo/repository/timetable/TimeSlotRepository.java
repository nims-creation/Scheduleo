package com.saas.Schedulo.repository.timetable;

import com.saas.Schedulo.entity.timetable.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
