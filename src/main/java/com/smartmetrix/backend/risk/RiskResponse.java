package com.smartmetrix.backend.risk;

import java.util.List;

public class RiskResponse {

    private Long instrumentId;

    private int riskScore;

    private String riskLevel;

    private double driftScore;

    private long totalTestRecords;

    private long failTestRecords;

    private String environmentStatus;

    private Double temperature;

    private Double humidity;

    private Double vibration;

    private List<String> reasons;

    private String recommendation;

    public RiskResponse() {
    }

    public RiskResponse(
            Long instrumentId,
            int riskScore,
            String riskLevel,
            double driftScore,
            long totalTestRecords,
            long failTestRecords,
            String environmentStatus,
            Double temperature,
            Double humidity,
            Double vibration,
            List<String> reasons,
            String recommendation) {

        this.instrumentId = instrumentId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.driftScore = driftScore;
        this.totalTestRecords = totalTestRecords;
        this.failTestRecords = failTestRecords;
        this.environmentStatus = environmentStatus;
        this.temperature = temperature;
        this.humidity = humidity;
        this.vibration = vibration;
        this.reasons = reasons;
        this.recommendation = recommendation;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public double getDriftScore() {
        return driftScore;
    }

    public long getTotalTestRecords() {
        return totalTestRecords;
    }

    public long getFailTestRecords() {
        return failTestRecords;
    }

    public String getEnvironmentStatus() {
        return environmentStatus;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Double getVibration() {
        return vibration;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getRecommendation() {
        return recommendation;
    }
}