package com.smartmetrix.backend.drift;

public class DriftPoint {

    private Long testRecordId;
    private Long inspectionId;

    private Double referenceWeight;
    private Double observedWeight;
    private Double error;

    private String result;

    public DriftPoint() {
    }

    public DriftPoint(
            Long testRecordId,
            Long inspectionId,
            Double referenceWeight,
            Double observedWeight,
            Double error,
            String result) {

        this.testRecordId = testRecordId;
        this.inspectionId = inspectionId;
        this.referenceWeight = referenceWeight;
        this.observedWeight = observedWeight;
        this.error = error;
        this.result = result;
    }

    public Long getTestRecordId() {
        return testRecordId;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public Double getObservedWeight() {
        return observedWeight;
    }

    public Double getError() {
        return error;
    }

    public String getResult() {
        return result;
    }
}