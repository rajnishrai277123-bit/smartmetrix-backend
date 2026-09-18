package com.smartmetrix.backend.analytics;

import java.util.List;

public class AdvancedAnalyticsResponse {

    private Long instrumentId;

    private double healthScore;
    private String healthStatus;

    private double environmentImpactScore;
    private String environmentImpactLevel;

    private double averageError;
    private double maximumAbsoluteError;
    private long totalTests;
    private long failedTests;

    private String recalibrationPrediction;
    private String recalibrationRecommendation;

    private List<TrendPoint> trend;

    public AdvancedAnalyticsResponse(
            Long instrumentId,
            double healthScore,
            String healthStatus,
            double environmentImpactScore,
            String environmentImpactLevel,
            double averageError,
            double maximumAbsoluteError,
            long totalTests,
            long failedTests,
            String recalibrationPrediction,
            String recalibrationRecommendation,
            List<TrendPoint> trend) {

        this.instrumentId = instrumentId;
        this.healthScore = healthScore;
        this.healthStatus = healthStatus;
        this.environmentImpactScore = environmentImpactScore;
        this.environmentImpactLevel = environmentImpactLevel;
        this.averageError = averageError;
        this.maximumAbsoluteError = maximumAbsoluteError;
        this.totalTests = totalTests;
        this.failedTests = failedTests;
        this.recalibrationPrediction = recalibrationPrediction;
        this.recalibrationRecommendation = recalibrationRecommendation;
        this.trend = trend;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public double getHealthScore() {
        return healthScore;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public double getEnvironmentImpactScore() {
        return environmentImpactScore;
    }

    public String getEnvironmentImpactLevel() {
        return environmentImpactLevel;
    }

    public double getAverageError() {
        return averageError;
    }

    public double getMaximumAbsoluteError() {
        return maximumAbsoluteError;
    }

    public long getTotalTests() {
        return totalTests;
    }

    public long getFailedTests() {
        return failedTests;
    }

    public String getRecalibrationPrediction() {
        return recalibrationPrediction;
    }

    public String getRecalibrationRecommendation() {
        return recalibrationRecommendation;
    }

    public List<TrendPoint> getTrend() {
        return trend;
    }

    public static class TrendPoint {

        private String date;
        private double error;
        private String result;

        public TrendPoint(
                String date,
                double error,
                String result) {

            this.date = date;
            this.error = error;
            this.result = result;
        }

        public String getDate() {
            return date;
        }

        public double getError() {
            return error;
        }

        public String getResult() {
            return result;
        }
    }
}