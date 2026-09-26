package com.smartmetrix.backend.instrument.dto;

public class InstrumentHealthResponse {

    private Long instrumentId;
    private String serialNumber;
    private String model;

    private int totalInspections;
    private int passedInspections;
    private int failedInspections;

    private double passRate;

    private double wpQuality;
    private double repeatabilityQuality;
    private double eccentricityQuality;

    private double healthScore;
    private String healthStatus;

    public InstrumentHealthResponse() {
    }

    public InstrumentHealthResponse(
            Long instrumentId,
            String serialNumber,
            String model,
            int totalInspections,
            int passedInspections,
            int failedInspections,
            double passRate,
            double wpQuality,
            double repeatabilityQuality,
            double eccentricityQuality,
            double healthScore,
            String healthStatus) {

        this.instrumentId = instrumentId;
        this.serialNumber = serialNumber;
        this.model = model;

        this.totalInspections = totalInspections;
        this.passedInspections = passedInspections;
        this.failedInspections = failedInspections;

        this.passRate = passRate;

        this.wpQuality = wpQuality;
        this.repeatabilityQuality = repeatabilityQuality;
        this.eccentricityQuality = eccentricityQuality;

        this.healthScore = healthScore;
        this.healthStatus = healthStatus;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModel() {
        return model;
    }

    public int getTotalInspections() {
        return totalInspections;
    }

    public int getPassedInspections() {
        return passedInspections;
    }

    public int getFailedInspections() {
        return failedInspections;
    }

    public double getPassRate() {
        return passRate;
    }

    public double getWpQuality() {
        return wpQuality;
    }

    public double getRepeatabilityQuality() {
        return repeatabilityQuality;
    }

    public double getEccentricityQuality() {
        return eccentricityQuality;
    }

    public double getHealthScore() {
        return healthScore;
    }

    public String getHealthStatus() {
        return healthStatus;
    }
}