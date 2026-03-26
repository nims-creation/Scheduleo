package com.saas.Schedulo.repository.user;

import com.saas.Schedulo.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    List<Role> findByIsSystemRoleTrue();
    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNames(@Param("names") List<String> names);
}
