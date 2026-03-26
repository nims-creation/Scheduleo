package com.saas.Schedulo.repository.user;

import com.saas.Schedulo.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.isDeleted = false")
    Page<User> findByOrganizationId(@Param("orgId") UUID organizationId, Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isDeleted = false")
    List<User> findByRoleName(@Param("roleName") String roleName);
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND u.isDeleted = false")
    Page<User> searchByOrganization(
            @Param("orgId") UUID organizationId,
            @Param("search") String search,
            Pageable pageable
    );
    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND u.isActive = true AND u.isDeleted = false")
    long countActiveByOrganization(@Param("orgId") UUID organizationId);
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") UUID userId);
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountLockedUntil = null WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") UUID userId);
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil < :now")
    List<User> findExpiredLocks(@Param("now") LocalDateTime now);
}