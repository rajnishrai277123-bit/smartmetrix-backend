package com.smartmetrix.backend.drift;

import java.util.List;

public class DriftAnalysisResponse {

    private Long instrumentId;

    private int totalRecords;

    private Double averageError;

    private Double averageAbsoluteError;

    private Double maximumAbsoluteError;

    private Double driftScore;

    private String driftStatus;

    private String explanation;

    private List<DriftPoint> history;

    public DriftAnalysisResponse() {
    }

    public DriftAnalysisResponse(
            Long instrumentId,
            int totalRecords,
            Double averageError,
            Double averageAbsoluteError,
            Double maximumAbsoluteError,
            Double driftScore,
            String driftStatus,
            String explanation,
            List<DriftPoint> history) {

        this.instrumentId = instrumentId;
        this.totalRecords = totalRecords;
        this.averageError = averageError;
        this.averageAbsoluteError = averageAbsoluteError;
        this.maximumAbsoluteError = maximumAbsoluteError;
        this.driftScore = driftScore;
        this.driftStatus = driftStatus;
        this.explanation = explanation;
        this.history = history;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public Double getAverageError() {
        return averageError;
    }

    public Double getAverageAbsoluteError() {
        return averageAbsoluteError;
    }

    public Double getMaximumAbsoluteError() {
        return maximumAbsoluteError;
    }

    public Double getDriftScore() {
        return driftScore;
    }

    public String getDriftStatus() {
        return driftStatus;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<DriftPoint> getHistory() {
        return history;
    }
}