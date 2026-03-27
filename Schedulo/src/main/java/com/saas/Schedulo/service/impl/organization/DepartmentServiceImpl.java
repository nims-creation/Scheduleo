package com.saas.Schedulo.service.impl.organization;

import com.saas.Schedulo.dto.request.organization.CreateDepartmentRequest;
import com.saas.Schedulo.dto.request.organization.UpdateDepartmentRequest;
import com.saas.Schedulo.dto.response.organization.DepartmentResponse;
import com.saas.Schedulo.entity.organization.Department;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.resource.ResourceConflictException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.DepartmentRepository;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.organization.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getByOrganization(UUID organizationId) {
        List<Department> departments = departmentRepository
                .findByOrganizationIdAndParentDepartmentIsNullOrderBySortOrderAsc(organizationId);
        return departments.stream()
                .map(this::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(UUID departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId.toString()));
        return toDepartmentResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse create(UUID organizationId, CreateDepartmentRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));

        if (departmentRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new ResourceConflictException("A department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .color(request.getColor())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .organization(organization)
                .build();

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getParentDepartmentId().toString()));
            department.setParentDepartment(parent);
        }

        if (request.getHeadId() != null) {
            User head = userRepository.findById(request.getHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getHeadId().toString()));
            department.setHead(head);
        }

        Department saved = departmentRepository.save(department);
        log.info("Created department '{}' for org {}", saved.getName(), organizationId);
        return toDepartmentResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(UUID departmentId, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId.toString()));

        if (request.getName() != null && !request.getName().isBlank()) {
            department.setName(request.getName());
        }
        if (request.getCode() != null) {
            department.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            department.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getParentDepartmentId().toString()));
            department.setParentDepartment(parent);
        }
        if (request.getHeadId() != null) {
            User head = userRepository.findById(request.getHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getHeadId().toString()));
            department.setHead(head);
        }

        Department updated = departmentRepository.save(department);
        log.info("Updated department '{}'", departmentId);
        return toDepartmentResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId.toString()));
        departmentRepository.delete(department);
        log.info("Deleted department '{}'", departmentId);
    }

    private DepartmentResponse toDepartmentResponse(Department dept) {
        List<DepartmentResponse> sub = dept.getSubDepartments() != null
                ? dept.getSubDepartments().stream()
                    .map(this::toDepartmentResponseSimple)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .color(dept.getColor())
                .sortOrder(dept.getSortOrder())
                .organizationId(dept.getOrganization() != null ? dept.getOrganization().getId() : null)
                .parentDepartmentId(dept.getParentDepartment() != null ? dept.getParentDepartment().getId() : null)
                .parentDepartmentName(dept.getParentDepartment() != null ? dept.getParentDepartment().getName() : null)
                .headId(dept.getHead() != null ? dept.getHead().getId() : null)
                .headName(dept.getHead() != null ? dept.getHead().getFirstName() + " " + dept.getHead().getLastName() : null)
                .subDepartments(sub)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }

    // Simpler version without sub-departments to avoid deep recursion
    private DepartmentResponse toDepartmentResponseSimple(Department dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .color(dept.getColor())
                .sortOrder(dept.getSortOrder())
                .organizationId(dept.getOrganization() != null ? dept.getOrganization().getId() : null)
                .headId(dept.getHead() != null ? dept.getHead().getId() : null)
                .headName(dept.getHead() != null ? dept.getHead().getFirstName() + " " + dept.getHead().getLastName() : null)
                .subDepartments(Collections.emptyList())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
