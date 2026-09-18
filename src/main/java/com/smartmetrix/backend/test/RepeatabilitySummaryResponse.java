package com.smartmetrix.backend.test;

import java.util.List;

public class RepeatabilitySummaryResponse {

    private Long testRunId;
    private Long inspectionId;
    private Double referenceWeight;
    private List<Double> readings;
    private Double average;
    private Double averageError;
    private Double range;
    private String result;

    public RepeatabilitySummaryResponse() {
    }

    public RepeatabilitySummaryResponse(
            Long testRunId,
            Long inspectionId,
            Double referenceWeight,
            List<Double> readings,
            Double average,
            Double averageError,
            Double range,
            String result) {

        this.testRunId = testRunId;
        this.inspectionId = inspectionId;
        this.referenceWeight = referenceWeight;
        this.readings = readings;
        this.average = average;
        this.averageError = averageError;
        this.range = range;
        this.result = result;
    }

    public Long getTestRunId() {
        return testRunId;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Double getReferenceWeight() {
        return referenceWeight;
    }

    public List<Double> getReadings() {
        return readings;
    }

    public Double getAverage() {
        return average;
    }

    public Double getAverageError() {
        return averageError;
    }

    public Double getRange() {
        return range;
    }

    public String getResult() {
        return result;
    }
}