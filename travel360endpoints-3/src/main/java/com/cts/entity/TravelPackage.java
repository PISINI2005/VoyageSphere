package com.cts.entity;

import com.cts.enums.PackageStatus;
import com.cts.enums.TravelPackageCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    private String packageName;

    private String source;

    private String destination;

    private double price;

    private int durationDays;

    private int totalSlots;

    private String description;

    @Enumerated(EnumType.STRING)
    private TravelPackageCategory category;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String dayWisePlan;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;
}
