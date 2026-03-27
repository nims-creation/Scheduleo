package com.saas.Schedulo.service.organization;

import com.saas.Schedulo.dto.request.organization.CreateDepartmentRequest;
import com.saas.Schedulo.dto.request.organization.UpdateDepartmentRequest;
import com.saas.Schedulo.dto.response.organization.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    List<DepartmentResponse> getByOrganization(UUID organizationId);
    DepartmentResponse getById(UUID departmentId);
    DepartmentResponse create(UUID organizationId, CreateDepartmentRequest request);
    DepartmentResponse update(UUID departmentId, UpdateDepartmentRequest request);
    void delete(UUID departmentId);
}
