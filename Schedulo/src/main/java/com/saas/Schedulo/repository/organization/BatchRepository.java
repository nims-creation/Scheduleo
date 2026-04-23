package com.saas.Schedulo.repository.organization;

import com.saas.Schedulo.entity.organization.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByDepartmentIdOrderByNameAsc(UUID departmentId);

    List<Batch> findByDepartmentIdAndSemesterOrderByNameAsc(UUID departmentId, Integer semester);

    boolean existsByNameAndDepartmentId(String name, UUID departmentId);
}
