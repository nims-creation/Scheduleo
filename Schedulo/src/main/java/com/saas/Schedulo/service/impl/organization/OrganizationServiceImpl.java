package com.saas.Schedulo.service.impl.organization;

import com.saas.Schedulo.dto.mapper.OrganizationMapper;
import com.saas.Schedulo.dto.request.organization.UpdateOrganizationRequest;
import com.saas.Schedulo.dto.response.organization.OrganizationResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.service.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));
        return organizationMapper.toOrganizationResponse(organization);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(UUID organizationId, UpdateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));

        if (request.getName() != null && !request.getName().isBlank()) {
            organization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getOrganizationType() != null) {
            organization.setOrganizationType(request.getOrganizationType());
        }
        if (request.getEmail() != null) {
            organization.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            organization.setPhone(request.getPhone());
        }
        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            organization.setTimezone(request.getTimezone());
        }
        if (request.getWorkingDays() != null) {
            organization.setWorkingDays(request.getWorkingDays());
        }
        if (request.getWorkingHoursStart() != null) {
            organization.setWorkingHoursStart(request.getWorkingHoursStart());
        }
        if (request.getWorkingHoursEnd() != null) {
            organization.setWorkingHoursEnd(request.getWorkingHoursEnd());
        }
        if (request.getSlotDurationMinutes() != null) {
            organization.setSlotDurationMinutes(request.getSlotDurationMinutes());
        }

        if (request.getSettings() != null) {
            Map<String, Object> currentSettings = organization.getSettings();
            if (currentSettings == null) {
                currentSettings = new HashMap<>();
            }
            currentSettings.putAll(request.getSettings());
            organization.setSettings(currentSettings);
        }

        Organization updatedOrganization = organizationRepository.save(organization);
        log.info("Updated organization with ID: {}", organizationId);

        return organizationMapper.toOrganizationResponse(updatedOrganization);
    }
}
