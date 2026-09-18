package com.smartmetrix.backend.ml;

import java.util.List;

public class MlPredictionResponse {

    private Long instrumentId;
    private String predictionStatus;

    private double currentAverageError;
    private double predictedNextError;

    private double trendSlope;
    private double confidence;

    private String predictedRisk;
    private String explanation;

    private List<PredictionPoint> history;

    public MlPredictionResponse(
            Long instrumentId,
            String predictionStatus,
            double currentAverageError,
            double predictedNextError,
            double trendSlope,
            double confidence,
            String predictedRisk,
            String explanation,
            List<PredictionPoint> history) {

        this.instrumentId = instrumentId;
        this.predictionStatus = predictionStatus;
        this.currentAverageError = currentAverageError;
        this.predictedNextError = predictedNextError;
        this.trendSlope = trendSlope;
        this.confidence = confidence;
        this.predictedRisk = predictedRisk;
        this.explanation = explanation;
        this.history = history;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public String getPredictionStatus() {
        return predictionStatus;
    }

    public double getCurrentAverageError() {
        return currentAverageError;
    }

    public double getPredictedNextError() {
        return predictedNextError;
    }

    public double getTrendSlope() {
        return trendSlope;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getPredictedRisk() {
        return predictedRisk;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<PredictionPoint> getHistory() {
        return history;
    }
}