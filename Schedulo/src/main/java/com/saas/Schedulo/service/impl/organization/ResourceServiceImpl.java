package com.saas.Schedulo.service.impl.organization;

import com.saas.Schedulo.dto.request.organization.CreateResourceRequest;
import com.saas.Schedulo.dto.response.organization.ResourceResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.organization.Resource;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.organization.ResourceRepository;
import com.saas.Schedulo.service.organization.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public ResourceResponse createResource(CreateResourceRequest request, UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));

        if (resourceRepository.existsByNameAndOrganizationIdAndIsDeletedFalse(request.getName(), organizationId)) {
            throw new IllegalArgumentException("Resource with this name already exists in the organization");
        }

        Resource resource = Resource.builder()
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .isAvailableForBooking(request.getIsAvailableForBooking() != null ? request.getIsAvailableForBooking() : true)
                .organization(organization)
                .build();

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(UUID id, CreateResourceRequest request, UUID organizationId) {
        Resource resource = getValidResource(id, organizationId);
        
        if (!resource.getName().equals(request.getName()) && 
            resourceRepository.existsByNameAndOrganizationIdAndIsDeletedFalse(request.getName(), organizationId)) {
            throw new IllegalArgumentException("Resource with this name already exists in the organization");
        }

        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setCapacity(request.getCapacity());
        if (request.getIsAvailableForBooking() != null) {
             resource.setIsAvailableForBooking(request.getIsAvailableForBooking());
        }

        return mapToResponse(resourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void deleteResource(UUID id, UUID organizationId) {
        Resource resource = getValidResource(id, organizationId);
        resource.setIsDeleted(true);
        resourceRepository.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(UUID id, UUID organizationId) {
        return mapToResponse(getValidResource(id, organizationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources(UUID organizationId) {
        return resourceRepository.findByOrganizationIdAndIsDeletedFalse(organizationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Resource getValidResource(UUID id, UUID organizationId) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id.toString()));
        if (resource.getIsDeleted() || !resource.getOrganization().getId().equals(organizationId)) {
             throw new ResourceNotFoundException("Resource", "id", id.toString());
        }
        return resource;
    }

    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .capacity(resource.getCapacity())
                .isAvailableForBooking(resource.getIsAvailableForBooking())
                .organizationId(resource.getOrganization().getId())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
