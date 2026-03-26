package com.saas.Schedulo.repository.timetable;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Page<Resource> findByOrganizationIdAndIsDeletedFalse(UUID organizationId, Pageable pageable);
    List<Resource> findByOrganizationIdAndResourceTypeAndIsDeletedFalse(
            UUID organizationId,
            Resource.ResourceType resourceType
    );
    @Query("SELECT r FROM Resource r WHERE r.organization.id = :orgId " +
            "AND r.isBookable = true AND r.isDeleted = false " +
            "AND r.id NOT IN (" +
            "  SELECT se.resource.id FROM ScheduleEntry se " +
            "  WHERE se.scheduleDate = :date " +
            "  AND ((se.startDatetime < :endTime AND se.endDatetime > :startTime)) " +
            "  AND se.isDeleted = false" +
            ")")
    List<Resource> findAvailableResources(
            @Param("orgId") UUID organizationId,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime
    );
    @Query("SELECT r FROM Resource r WHERE r.organization.id = :orgId " +
            "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND r.isDeleted = false")
    Page<Resource> searchByOrganization(
            @Param("orgId") UUID organizationId,
            @Param("search") String search,
            Pageable pageable
    );
}
