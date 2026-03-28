package com.saas.Schedulo.entity.organization;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "OrgResource")
@Table(name = "resources", indexes = {
        @Index(name = "idx_resource_org", columnList = "organization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResourceType type;

    @Column(name = "capacity")
    private Integer capacity;

    @Builder.Default
    @Column(name = "is_available_for_booking")
    private Boolean isAvailableForBooking = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    public enum ResourceType {
        ROOM, LAB, EQUIPMENT, VEHICLE, OTHER
    }
}
