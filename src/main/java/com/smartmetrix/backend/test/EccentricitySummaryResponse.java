package com.smartmetrix.backend.test;

import java.util.List;

public class EccentricitySummaryResponse {

    private Long inspectionId;

    private Double referenceWeight;

    private List<EccentricityPositionResult> positions;

    private Double maximumDifference;

    private Double mpe;

    private String result;

    public EccentricitySummaryResponse() {
    }

    public EccentricitySummaryResponse(
            Long inspectionId,
            Double referenceWeight,
            List<EccentricityPositionResult> positions,
            Double maximumDifference,
            Double mpe,
            String result) {

        this.inspectionId = inspectionId;
        this.referenceWeight = referenceWeight;
        this.positions = positions;
        this.maximumDifference = maximumDifference;
        this.mpe = mpe;
        this.result = result;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public List<EccentricityPositionResult> getPositions() {
        return positions;
    }

    public Double getMaximumDifference() {
        return maximumDifference;
    }

    public Double getMpe() {
        return mpe;
    }

    public String getResult() {
        return result;
    }
}