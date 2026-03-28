package com.saas.Schedulo.repository.organization;

import com.saas.Schedulo.entity.organization.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("orgResourceRepository")
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    List<Resource> findByOrganizationIdAndIsDeletedFalse(UUID organizationId);
    boolean existsByNameAndOrganizationIdAndIsDeletedFalse(String name, UUID organizationId);
}
