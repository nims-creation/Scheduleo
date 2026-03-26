package com.saas.Schedulo.dto.mapper;

import com.saas.Schedulo.dto.request.timetable.CreateTimetableRequest;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;
import com.saas.Schedulo.dto.response.timetable.TimeSlotResponse;
import com.saas.Schedulo.dto.response.timetable.TimetableResponse;
import com.saas.Schedulo.entity.timetable.ScheduleEntry;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.entity.timetable.Timetable;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimetableMapper {


    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "timeSlots", ignore = true)
    @Mapping(target = "scheduleEntries", ignore = true)
    Timetable toEntity(CreateTimetableRequest request);

    @Mapping(target = "department", source = "department")
    TimetableResponse toResponse(Timetable timetable);

    List<TimetableResponse> toResponseList(List<Timetable> timetables);

    @Mapping(target = "durationMinutes", expression = "java(timeSlot.getDurationMinutes())")
    TimeSlotResponse toTimeSlotResponse(TimeSlot timeSlot);

    @Mapping(target = "timeSlot", source = "timeSlot")
    @Mapping(target = "resource", source = "resource")
    @Mapping(target = "assignedTo", source = "assignedTo")
    ScheduleEntryResponse toScheduleEntryResponse(ScheduleEntry entry);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTimetableFromRequest(CreateTimetableRequest request, @MappingTarget Timetable timetable);
}

