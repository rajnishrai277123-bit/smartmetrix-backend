package com.smartmetrix.backend.test;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "test_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_test_records_client_record_id",
                        columnNames = "client_record_id"
                )
        }
)
public class TestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_record_id", unique = true)
    private String clientRecordId;

    private Long inspectionId;

    private String testType;

    private Double referenceWeight;

    private Double observedWeight;

    private Double error;

    private Double mpe;

    private Double temperature;

    private Double humidity;

    private Double vibration;

    private String result;

    private String testStage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TestRecord() {
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

    public String getClientRecordId() {
        return clientRecordId;
    }

    public void setClientRecordId(String clientRecordId) {
        this.clientRecordId = clientRecordId;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
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

    public Double getError() {
        return error;
    }

    public void setError(Double error) {
        this.error = error;
    }

    public Double getMpe() {
        return mpe;
    }

    public void setMpe(Double mpe) {
        this.mpe = mpe;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getVibration() {
        return vibration;
    }

    public void setVibration(Double vibration) {
        this.vibration = vibration;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTestStage() {
        return testStage;
    }

    public void setTestStage(String testStage) {
        this.testStage = testStage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}