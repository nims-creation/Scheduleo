package com.saas.Schedulo.entity.timetable;

import com.saas.Schedulo.entity.base.BaseEntity;
import com.saas.Schedulo.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources", indexes = {
        @Index(name = "idx_resource_org", columnList = "organization_id"),
        @Index(name = "idx_resource_type", columnList = "resource_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "floor")
    private String floor;

    @Column(name = "building")
    private String building;

    @Column(name = "amenities", columnDefinition = "JSON")
    private String amenities;

    @Column(name = "is_bookable", nullable = false)
    @Builder.Default
    private Boolean isBookable = true;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "color", length = 7)
    private String color;

    public enum ResourceType {
        CLASSROOM, LAB, AUDITORIUM, MEETING_ROOM,
        CONFERENCE_ROOM, EQUIPMENT, VEHICLE,
        CONSULTATION_ROOM, OPERATION_THEATER, OTHER
    }
}

