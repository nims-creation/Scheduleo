package com.saas.Schedulo.service.organization;

import com.saas.Schedulo.dto.request.organization.CreateResourceRequest;
import com.saas.Schedulo.dto.response.organization.ResourceResponse;

import java.util.List;
import java.util.UUID;

public interface ResourceService {
    ResourceResponse createResource(CreateResourceRequest request, UUID organizationId);
    ResourceResponse updateResource(UUID id, CreateResourceRequest request, UUID organizationId);
    void deleteResource(UUID id, UUID organizationId);
    ResourceResponse getResourceById(UUID id, UUID organizationId);
    List<ResourceResponse> getAllResources(UUID organizationId);
}
