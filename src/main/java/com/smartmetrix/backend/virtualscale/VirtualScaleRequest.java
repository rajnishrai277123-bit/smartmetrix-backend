package com.smartmetrix.backend.virtualscale;

public class VirtualScaleRequest {

    private Long inspectionId;

    // Gross/reference weight placed on the scale
    private Double referenceWeight;

    // Weight of empty container/platform
    private Double tareWeight;

    private String testStage;

    public VirtualScaleRequest() {
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public void setReferenceWeight(Double referenceWeight) {
        this.referenceWeight = referenceWeight;
    }

    public Double getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(Double tareWeight) {
        this.tareWeight = tareWeight;
    }

    public String getTestStage() {
        return testStage;
    }

    public void setTestStage(String testStage) {
        this.testStage = testStage;
    }
}