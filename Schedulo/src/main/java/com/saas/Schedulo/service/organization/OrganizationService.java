package com.saas.Schedulo.service.organization;

import com.saas.Schedulo.dto.request.organization.UpdateOrganizationRequest;
import com.saas.Schedulo.dto.response.organization.OrganizationResponse;

import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse getOrganizationById(UUID organizationId);
    OrganizationResponse updateOrganization(UUID organizationId, UpdateOrganizationRequest request);
}
