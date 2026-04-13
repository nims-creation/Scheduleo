package com.saas.Schedulo.service.impl.timetable;

import com.saas.Schedulo.dto.mapper.TimetableMapper;
import com.saas.Schedulo.dto.request.timetable.CreateTimetableRequest;
import com.saas.Schedulo.dto.request.timetable.TimeSlotRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.timetable.TimetableResponse;
import com.saas.Schedulo.entity.organization.Department;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.entity.timetable.Timetable;
import com.saas.Schedulo.exception.resource.ResourceConflictException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.DepartmentRepository;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.timetable.TimeSlotRepository;
import com.saas.Schedulo.repository.timetable.TimetableRepository;
import com.saas.Schedulo.service.email.EmailService;
import com.saas.Schedulo.service.timetable.TimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TimetableMapper timetableMapper;
    private final EmailService emailService;

    @Override
    public TimetableResponse create(CreateTimetableRequest request, UUID organizationId) {
        log.info("Creating timetable for organization: {}", organizationId);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        Timetable timetable = timetableMapper.toEntity(request);
        timetable.setOrganization(organization);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            timetable.setDepartment(department);
        }

        // Build time slots before saving — one flush handles everything
        if (request.getTimeSlots() != null && !request.getTimeSlots().isEmpty()) {
            for (int i = 0; i < request.getTimeSlots().size(); i++) {
                TimeSlotRequest slotRequest = request.getTimeSlots().get(i);
                TimeSlot timeSlot = TimeSlot.builder()
                        .timetable(timetable)
                        .slotName(slotRequest.getSlotName())
                        .startTime(slotRequest.getStartTime())
                        .endTime(slotRequest.getEndTime())
                        .dayOfWeek(slotRequest.getDayOfWeek())
                        .slotType(slotRequest.getSlotType() != null ?
                                TimeSlot.SlotType.valueOf(slotRequest.getSlotType()) :
                                TimeSlot.SlotType.REGULAR)
                        .breakAfter(slotRequest.getBreakAfter())
                        .breakDurationMinutes(slotRequest.getBreakDurationMinutes())
                        .sortOrder(slotRequest.getSortOrder() != null ? slotRequest.getSortOrder() : i)
                        .build();
                timetable.getTimeSlots().add(timeSlot);
            }
        }

        timetable = timetableRepository.save(timetable);
        log.info("Timetable created with ID: {}", timetable.getId());
        return timetableMapper.toResponse(timetable);
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse getById(UUID id) {
        Timetable timetable = timetableRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));
        return timetableMapper.toResponse(timetable);
    }

    @Override
    public TimetableResponse update(UUID id, CreateTimetableRequest request) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));

        if (timetable.getStatus() == Timetable.TimetableStatus.PUBLISHED) {
            throw new ResourceConflictException("Cannot update a published timetable. Archive it first.");
        }

        timetableMapper.updateTimetableFromRequest(request, timetable);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            timetable.setDepartment(department);
        }

        timetable = timetableRepository.save(timetable);
        return timetableMapper.toResponse(timetable);
    }

    @Override
    public void delete(UUID id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));

        if (timetable.getStatus() == Timetable.TimetableStatus.PUBLISHED) {
            throw new ResourceConflictException("Cannot delete a published timetable. Archive it first.");
        }

        timetable.setIsDeleted(true);
        timetableRepository.save(timetable);
        log.info("Timetable soft deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TimetableResponse> getByOrganization(UUID organizationId, Pageable pageable) {
        Page<Timetable> page = timetableRepository.findByOrganizationId(organizationId, pageable);
        Page<TimetableResponse> responsePage = page.map(timetableMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> getActiveByDate(UUID organizationId, LocalDate date) {
        List<Timetable> timetables = timetableRepository.findActiveByOrganizationAndDate(organizationId, date);
        return timetableMapper.toResponseList(timetables);
    }

    @Override
    public TimetableResponse publish(UUID id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));

        if (timetable.getStatus() == Timetable.TimetableStatus.PUBLISHED) {
            throw new ResourceConflictException("Timetable is already published");
        }

        // Check for overlapping published timetables
        boolean hasOverlap = timetableRepository.existsOverlappingPublished(
                timetable.getOrganization().getId(),
                timetable.getEffectiveFrom(),
                timetable.getEffectiveTo() != null ? timetable.getEffectiveTo() : LocalDate.MAX,
                timetable.getId()
        );

        if (hasOverlap) {
            throw new ResourceConflictException(
                    "Another timetable is already published for the overlapping date range"
            );
        }

        timetable.setStatus(Timetable.TimetableStatus.PUBLISHED);
        timetable = timetableRepository.save(timetable);

        log.info("Timetable published: {}", id);
        emailService.sendTimetablePublishedEmail(timetable);
        return timetableMapper.toResponse(timetable);
    }

    @Override
    public TimetableResponse archive(UUID id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));

        timetable.setStatus(Timetable.TimetableStatus.ARCHIVED);
        timetable = timetableRepository.save(timetable);

        log.info("Timetable archived: {}", id);
        return timetableMapper.toResponse(timetable);
    }

    @Override
    public TimetableResponse duplicateAsTemplate(UUID id, String templateName) {
        Timetable original = timetableRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", id));

        Timetable template = Timetable.builder()
                .name(templateName)
                .description(original.getDescription())
                .organization(original.getOrganization())
                .timetableType(original.getTimetableType())
                .effectiveFrom(LocalDate.now())
                .status(Timetable.TimetableStatus.DRAFT)
                .isTemplate(true)
                .templateName(templateName)
                .build();

        template = timetableRepository.save(template);

        // Copy time slots
        for (TimeSlot slot : original.getTimeSlots()) {
            TimeSlot newSlot = TimeSlot.builder()
                    .timetable(template)
                    .slotName(slot.getSlotName())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .dayOfWeek(slot.getDayOfWeek())
                    .slotType(slot.getSlotType())
                    .breakAfter(slot.getBreakAfter())
                    .breakDurationMinutes(slot.getBreakDurationMinutes())
                    .sortOrder(slot.getSortOrder())
                    .build();
            template.getTimeSlots().add(newSlot);
        }

        template = timetableRepository.save(template);
        log.info("Template created from timetable {}: {}", id, template.getId());
        return timetableMapper.toResponse(template);
    }

    @Override
    public TimetableResponse createFromTemplate(UUID templateId, CreateTimetableRequest request) {
        Timetable template = timetableRepository.findByIdWithDetails(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", templateId));

        if (!template.getIsTemplate()) {
            throw new ResourceConflictException("Specified timetable is not a template");
        }

        // Build new timetable entity directly from template — no DB round-trip needed
        Timetable newTimetable = Timetable.builder()
                .name(request.getName())
                .description(request.getDescription() != null ? request.getDescription() : template.getDescription())
                .organization(template.getOrganization())
                .timetableType(request.getTimetableType() != null ? request.getTimetableType() : template.getTimetableType())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(Timetable.TimetableStatus.DRAFT)
                .isTemplate(false)
                .build();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            newTimetable.setDepartment(department);
        }

        // Copy time slots from template before saving — single flush
        for (TimeSlot slot : template.getTimeSlots()) {
            TimeSlot newSlot = TimeSlot.builder()
                    .timetable(newTimetable)
                    .slotName(slot.getSlotName())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .dayOfWeek(slot.getDayOfWeek())
                    .slotType(slot.getSlotType())
                    .breakAfter(slot.getBreakAfter())
                    .breakDurationMinutes(slot.getBreakDurationMinutes())
                    .sortOrder(slot.getSortOrder())
                    .build();
            newTimetable.getTimeSlots().add(newSlot);
        }

        newTimetable = timetableRepository.save(newTimetable);
        log.info("Timetable created from template {}: {}", templateId, newTimetable.getId());
        return timetableMapper.toResponse(newTimetable);
    }
}

