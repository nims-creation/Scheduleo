package com.saas.Schedulo.repository.organization;

import com.saas.Schedulo.entity.organization.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByOrganizationIdOrderBySortOrderAsc(UUID organizationId);
    List<Department> findByOrganizationIdAndParentDepartmentIsNullOrderBySortOrderAsc(UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
}
