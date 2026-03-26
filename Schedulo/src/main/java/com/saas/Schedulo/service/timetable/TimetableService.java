package com.saas.Schedulo.service.timetable;

import com.saas.Schedulo.dto.request.timetable.CreateTimetableRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.timetable.TimetableResponse;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimetableService {
    TimetableResponse create(CreateTimetableRequest request, UUID organizationId);
    TimetableResponse getById(UUID id);
    TimetableResponse update(UUID id, CreateTimetableRequest request);
    void delete(UUID id);
    PagedResponse<TimetableResponse> getByOrganization(UUID organizationId, Pageable pageable);
    List<TimetableResponse> getActiveByDate(UUID organizationId, LocalDate date);
    TimetableResponse publish(UUID id);
    TimetableResponse archive(UUID id);
    TimetableResponse duplicateAsTemplate(UUID id, String templateName);
    TimetableResponse createFromTemplate(UUID templateId, CreateTimetableRequest request);
}
