package com.saas.Schedulo.service.organization;

import com.saas.Schedulo.dto.request.organization.CreateBatchRequest;
import com.saas.Schedulo.dto.request.organization.UpdateBatchRequest;
import com.saas.Schedulo.dto.response.organization.BatchResponse;

import java.util.List;
import java.util.UUID;

public interface BatchService {

    BatchResponse create(CreateBatchRequest request);

    BatchResponse getById(UUID batchId);

    List<BatchResponse> getByDepartment(UUID departmentId);

    List<BatchResponse> getByDepartmentAndSemester(UUID departmentId, Integer semester);

    BatchResponse update(UUID batchId, UpdateBatchRequest request);

    void delete(UUID batchId);
}
