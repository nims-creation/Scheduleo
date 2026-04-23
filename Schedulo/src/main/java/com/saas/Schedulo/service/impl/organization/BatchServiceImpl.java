package com.saas.Schedulo.service.impl.organization;

import com.saas.Schedulo.dto.mapper.BatchMapper;
import com.saas.Schedulo.dto.request.organization.CreateBatchRequest;
import com.saas.Schedulo.dto.request.organization.UpdateBatchRequest;
import com.saas.Schedulo.dto.response.organization.BatchResponse;
import com.saas.Schedulo.entity.organization.Batch;
import com.saas.Schedulo.entity.organization.Department;
import com.saas.Schedulo.exception.resource.ResourceConflictException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.BatchRepository;
import com.saas.Schedulo.repository.organization.DepartmentRepository;
import com.saas.Schedulo.service.organization.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final DepartmentRepository departmentRepository;
    private final BatchMapper batchMapper;

    @Override
    @Transactional
    public BatchResponse create(CreateBatchRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId().toString()));

        if (batchRepository.existsByNameAndDepartmentId(request.getName(), request.getDepartmentId())) {
            throw new ResourceConflictException(
                    "A batch with name '" + request.getName() + "' already exists in this department");
        }

        Batch batch = Batch.builder()
                .name(request.getName())
                .code(request.getCode())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .strength(request.getStrength())
                .description(request.getDescription())
                .department(department)
                .build();

        Batch saved = batchRepository.save(batch);
        log.info("Created batch '{}' for department '{}'", saved.getName(), department.getName());
        return batchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getById(UUID batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId.toString()));
        return batchMapper.toResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getByDepartment(UUID departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId.toString());
        }
        return batchRepository.findByDepartmentIdOrderByNameAsc(departmentId)
                .stream()
                .map(batchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getByDepartmentAndSemester(UUID departmentId, Integer semester) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId.toString());
        }
        return batchRepository.findByDepartmentIdAndSemesterOrderByNameAsc(departmentId, semester)
                .stream()
                .map(batchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BatchResponse update(UUID batchId, UpdateBatchRequest request) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId.toString()));

        if (request.getName() != null && !request.getName().isBlank()) {
            batch.setName(request.getName());
        }
        if (request.getCode() != null) {
            batch.setCode(request.getCode());
        }
        if (request.getSemester() != null) {
            batch.setSemester(request.getSemester());
        }
        if (request.getAcademicYear() != null) {
            batch.setAcademicYear(request.getAcademicYear());
        }
        if (request.getStrength() != null) {
            batch.setStrength(request.getStrength());
        }
        if (request.getDescription() != null) {
            batch.setDescription(request.getDescription());
        }

        Batch updated = batchRepository.save(batch);
        log.info("Updated batch '{}'", batchId);
        return batchMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId.toString()));
        batchRepository.delete(batch);
        log.info("Deleted batch '{}'", batchId);
    }
}
