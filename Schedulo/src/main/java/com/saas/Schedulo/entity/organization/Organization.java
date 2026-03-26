package com.saas.Schedulo.entity.organization;

import com.saas.Schedulo.entity.base.BaseEntity;
import com.saas.Schedulo.entity.subscription.Subscription;
import com.saas.Schedulo.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_org_slug", columnList = "slug"),
        @Index(name = "idx_org_type", columnList = "organization_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    private OrganizationType organizationType;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website")
    private String website;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Embedded
    private Address address;

    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    @Column(name = "working_days")
    private String workingDays = "MON,TUE,WED,THU,FRI";

    @Column(name = "working_hours_start")
    private String workingHoursStart = "08:00";

    @Column(name = "working_hours_end")
    private String workingHoursEnd = "18:00";

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();

    @OneToOne(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

    @Column(name = "max_users")
    private Integer maxUsers = 10;

    @Column(name = "max_schedules_per_day")
    private Integer maxSchedulesPerDay = 100;

    @Column(name = "settings", columnDefinition = "JSON")
    private String settings;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Address {
    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;
}

