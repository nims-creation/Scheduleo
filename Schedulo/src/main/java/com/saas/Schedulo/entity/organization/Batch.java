package com.saas.Schedulo.entity.organization;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "batches", indexes = {
        @Index(name = "idx_batch_department", columnList = "department_id"),
        @Index(name = "idx_batch_dept_semester", columnList = "department_id, semester, academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch extends BaseEntity {

    /**
     * Human-readable name, e.g. "CSE-A", "CSE-B", "CSE-1st-Sem-A"
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Short code, e.g. "A", "B", "1A"
     */
    @Column(name = "code", length = 20)
    private String code;

    /**
     * Current semester number (1-8 for a 4-year degree)
     */
    @Column(name = "semester")
    private Integer semester;

    /**
     * Academic year, e.g. 2024 (the year the batch joined)
     */
    @Column(name = "academic_year", length = 10)
    private String academicYear;

    /**
     * Number of students in this batch
     */
    @Column(name = "strength")
    private Integer strength;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
