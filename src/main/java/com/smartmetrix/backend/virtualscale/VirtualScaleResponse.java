package com.smartmetrix.backend.virtualscale;

public class VirtualScaleResponse {

    private Long inspectionId;

    private String instrumentClass;

    private Double capacity;

    private Double scaleInterval;

    private Double referenceWeight;

    private Double tareWeight;

    private Double netReferenceWeight;

    private Double observedWeight;

    private String status;

    public VirtualScaleResponse() {
    }

    public VirtualScaleResponse(
            Long inspectionId,
            String instrumentClass,
            Double capacity,
            Double scaleInterval,
            Double referenceWeight,
            Double tareWeight,
            Double netReferenceWeight,
            Double observedWeight,
            String status) {

        this.inspectionId = inspectionId;
        this.instrumentClass = instrumentClass;
        this.capacity = capacity;
        this.scaleInterval = scaleInterval;
        this.referenceWeight = referenceWeight;
        this.tareWeight = tareWeight;
        this.netReferenceWeight = netReferenceWeight;
        this.observedWeight = observedWeight;
        this.status = status;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public String getInstrumentClass() {
        return instrumentClass;
    }

    public Double getCapacity() {
        return capacity;
    }

    public Double getScaleInterval() {
        return scaleInterval;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public Double getTareWeight() {
        return tareWeight;
    }

    public Double getNetReferenceWeight() {
        return netReferenceWeight;
    }

    public Double getObservedWeight() {
        return observedWeight;
    }

    public String getStatus() {
        return status;
    }
}