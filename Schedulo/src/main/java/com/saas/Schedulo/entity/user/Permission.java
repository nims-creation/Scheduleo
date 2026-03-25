package com.saas.Schedulo.entity.user;

import com.timetable.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private PermissionAction action;

    public enum PermissionAction {
        CREATE, READ, UPDATE, DELETE, MANAGE
    }
}

