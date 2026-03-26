package com.saas.Schedulo.repository.calendar;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    @Query("SELECT h FROM Holiday h WHERE " +
            "(h.organization.id = :orgId OR h.organization IS NULL) " +
            "AND h.holidayDate BETWEEN :startDate AND :endDate " +
            "AND h.isDeleted = false ORDER BY h.holidayDate")
    List<Holiday> findByOrganizationAndDateRange(
            @Param("orgId") UUID organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT h FROM Holiday h WHERE " +
            "(h.organization.id = :orgId OR h.organization IS NULL) " +
            "AND h.holidayDate = :date AND h.isDeleted = false")
    List<Holiday> findByOrganizationAndDate(
            @Param("orgId") UUID organizationId,
            @Param("date") LocalDate date
    );
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h " +
            "WHERE (h.organization.id = :orgId OR h.organization IS NULL) " +
            "AND h.holidayDate = :date AND h.isDeleted = false")
    boolean isHoliday(
            @Param("orgId") UUID organizationId,
            @Param("date") LocalDate date
    );
    List<Holiday> findByHolidayTypeAndIsDeletedFalse(Holiday.HolidayType holidayType);
}
