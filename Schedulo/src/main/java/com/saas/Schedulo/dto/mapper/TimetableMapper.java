package com.saas.Schedulo.dto.mapper;

import com.timetable.dto.request.timetable.*;
import com.timetable.dto.response.timetable.*;
import com.timetable.entity.timetable.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimetableMapper {

    @Mapping(target = "id", ignore = true)
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

