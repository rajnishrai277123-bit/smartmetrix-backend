package com.smartmetrix.backend.analytics;

public class AnalyticsResponse {

    private long totalInspections;
    private long passInspections;
    private long failInspections;
    private long pendingInspections;

    private long totalTestRecords;
    private long passTestRecords;
    private long failTestRecords;

    private double inspectionPassPercentage;
    private double inspectionFailPercentage;

    private double testPassPercentage;
    private double testFailPercentage;

    public AnalyticsResponse() {
    }

    public AnalyticsResponse(
            long totalInspections,
            long passInspections,
            long failInspections,
            long pendingInspections,
            long totalTestRecords,
            long passTestRecords,
            long failTestRecords,
            double inspectionPassPercentage,
            double inspectionFailPercentage,
            double testPassPercentage,
            double testFailPercentage) {

        this.totalInspections = totalInspections;
        this.passInspections = passInspections;
        this.failInspections = failInspections;
        this.pendingInspections = pendingInspections;

        this.totalTestRecords = totalTestRecords;
        this.passTestRecords = passTestRecords;
        this.failTestRecords = failTestRecords;

        this.inspectionPassPercentage =
                inspectionPassPercentage;

        this.inspectionFailPercentage =
                inspectionFailPercentage;

        this.testPassPercentage =
                testPassPercentage;

        this.testFailPercentage =
                testFailPercentage;
    }

    public long getTotalInspections() {
        return totalInspections;
    }

    public long getPassInspections() {
        return passInspections;
    }

    public long getFailInspections() {
        return failInspections;
    }

    public long getPendingInspections() {
        return pendingInspections;
    }

    public long getTotalTestRecords() {
        return totalTestRecords;
    }

    public long getPassTestRecords() {
        return passTestRecords;
    }

    public long getFailTestRecords() {
        return failTestRecords;
    }

    public double getInspectionPassPercentage() {
        return inspectionPassPercentage;
    }

    public double getInspectionFailPercentage() {
        return inspectionFailPercentage;
    }

    public double getTestPassPercentage() {
        return testPassPercentage;
    }

    public double getTestFailPercentage() {
        return testFailPercentage;
    }
}