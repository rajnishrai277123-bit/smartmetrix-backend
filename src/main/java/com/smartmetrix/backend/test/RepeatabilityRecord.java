package com.smartmetrix.backend.test;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repeatability_records")
public class RepeatabilityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inspectionId;

    private Long testRunId;

    private Double referenceWeight;

    private Double observedWeight;

    private Integer readingNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public RepeatabilityRecord() {
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
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

    public Long getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(Long testRunId) {
        this.testRunId = testRunId;
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

    public Integer getReadingNumber() {
        return readingNumber;
    }

    public void setReadingNumber(Integer readingNumber) {
        this.readingNumber = readingNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}