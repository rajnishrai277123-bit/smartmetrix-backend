package com.smartmetrix.backend.ml;

public class PredictionPoint {

    private Long testRecordId;
    private double referenceWeight;
    private double observedWeight;
    private double error;

    public PredictionPoint(
            Long testRecordId,
            double referenceWeight,
            double observedWeight,
            double error) {

        this.testRecordId = testRecordId;
        this.referenceWeight = referenceWeight;
        this.observedWeight = observedWeight;
        this.error = error;
    }

    public Long getTestRecordId() {
        return testRecordId;
    }

    public double getReferenceWeight() {
        return referenceWeight;
    }

    public double getObservedWeight() {
        return observedWeight;
    }

    public double getError() {
        return error;
    }
}