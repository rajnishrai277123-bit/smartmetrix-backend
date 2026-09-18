package com.smartmetrix.backend.test;

public class EccentricityPositionResult {

    private String position;

    private Double referenceWeight;

    private Double observedWeight;

    private Double error;

    private String result;

    public EccentricityPositionResult() {
    }

    public EccentricityPositionResult(
            String position,
            Double referenceWeight,
            Double observedWeight,
            Double error,
            String result) {

        this.position = position;
        this.referenceWeight = referenceWeight;
        this.observedWeight = observedWeight;
        this.error = error;
        this.result = result;
    }

    public String getPosition() {
        return position;
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