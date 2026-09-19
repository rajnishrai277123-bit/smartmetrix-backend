package com.smartmetrix.backend.test;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "eccentricity_records")
public class EccentricityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inspectionId;

    private String position;

    private Double referenceWeight;

    private Double observedWeight;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EccentricityRecord() {
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public void setReferenceWeight(Double referenceWeight) {
        this.referenceWeight = referenceWeight;
    }

    public Double getObservedWeight() {
        return observedWeight;
    }

    public void setObservedWeight(Double observedWeight) {
        this.observedWeight = observedWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}