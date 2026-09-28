package com.smartmetrix.backend.report;

import java.time.LocalDateTime;
import java.util.List;

public class InspectionReportResponse {

    // =====================================================
    // INSPECTION DETAILS
    // =====================================================

    private Long inspectionId;
    private Long instrumentId;
    private Long inspectorId;

    private String inspectionStatus;
    private String overallResult;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // =====================================================
    // REPORT SECTIONS
    // =====================================================

    private InstrumentReport instrument;

    private List<TestRecordReport> testRecords;

    private List<EnvironmentReport> environmentRecords;

    private List<RepeatabilityReport> repeatabilityRecords;

    private List<EccentricityReport> eccentricityRecords;

    public InspectionReportResponse() {
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
    }

    public String getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public String getOverallResult() {
        return overallResult;
    }

    public void setOverallResult(String overallResult) {
        this.overallResult = overallResult;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public InstrumentReport getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentReport instrument) {
        this.instrument = instrument;
    }

    public List<TestRecordReport> getTestRecords() {
        return testRecords;
    }

    public void setTestRecords(List<TestRecordReport> testRecords) {
        this.testRecords = testRecords;
    }

    public List<EnvironmentReport> getEnvironmentRecords() {
        return environmentRecords;
    }

    public void setEnvironmentRecords(
            List<EnvironmentReport> environmentRecords
    ) {
        this.environmentRecords = environmentRecords;
    }

    public List<RepeatabilityReport> getRepeatabilityRecords() {
        return repeatabilityRecords;
    }

    public void setRepeatabilityRecords(
            List<RepeatabilityReport> repeatabilityRecords
    ) {
        this.repeatabilityRecords = repeatabilityRecords;
    }

    public List<EccentricityReport> getEccentricityRecords() {
        return eccentricityRecords;
    }

    public void setEccentricityRecords(
            List<EccentricityReport> eccentricityRecords
    ) {
        this.eccentricityRecords = eccentricityRecords;
    }

    // =====================================================
    // INSTRUMENT REPORT
    // =====================================================

    public static class InstrumentReport {

        private Long id;
        private String serialNumber;
        private String manufacturer;
        private String model;
        private String instrumentClass;
        private Double capacity;
        private Double scaleInterval;
        private Double minCapacity;
        private String status;

        public InstrumentReport(
                Long id,
                String serialNumber,
                String manufacturer,
                String model,
                String instrumentClass,
                Double capacity,
                Double scaleInterval,
                Double minCapacity,
                String status
        ) {
            this.id = id;
            this.serialNumber = serialNumber;
            this.manufacturer = manufacturer;
            this.model = model;
            this.instrumentClass = instrumentClass;
            this.capacity = capacity;
            this.scaleInterval = scaleInterval;
            this.minCapacity = minCapacity;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getModel() {
            return model;
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

        public Double getMinCapacity() {
            return minCapacity;
        }

        public String getStatus() {
            return status;
        }
    }

    // =====================================================
    // WEIGHING PERFORMANCE / TEST RECORD
    // =====================================================

    public static class TestRecordReport {

        private Long id;
        private String clientRecordId;
        private String testType;

        private Double referenceWeight;
        private Double observedWeight;
        private Double error;
        private Double mpe;

        private Double temperature;
        private Double humidity;
        private Double vibration;

        private String result;
        private String testStage;

        private LocalDateTime createdAt;

        public TestRecordReport(
                Long id,
                String clientRecordId,
                String testType,
                Double referenceWeight,
                Double observedWeight,
                Double error,
                Double mpe,
                Double temperature,
                Double humidity,
                Double vibration,
                String result,
                String testStage,
                LocalDateTime createdAt
        ) {
            this.id = id;
            this.clientRecordId = clientRecordId;
            this.testType = testType;
            this.referenceWeight = referenceWeight;
            this.observedWeight = observedWeight;
            this.error = error;
            this.mpe = mpe;
            this.temperature = temperature;
            this.humidity = humidity;
            this.vibration = vibration;
            this.result = result;
            this.testStage = testStage;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public String getClientRecordId() {
            return clientRecordId;
        }

        public String getTestType() {
            return testType;
        }

        public Double getReferenceWeight() {
            return referenceWeight;
        }

        public Double getObservedWeight() {
            return observedWeight;
        }

        public Double getError() {
            return error;
        }

        public Double getMpe() {
            return mpe;
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

        public String getResult() {
            return result;
        }

        public String getTestStage() {
            return testStage;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    // =====================================================
    // ENVIRONMENT REPORT
    // =====================================================

    public static class EnvironmentReport {

        private Long id;
        private Double temperature;
        private Double humidity;
        private Double vibration;
        private String source;
        private String status;

        public EnvironmentReport(
                Long id,
                Double temperature,
                Double humidity,
                Double vibration,
                String source,
                String status
        ) {
            this.id = id;
            this.temperature = temperature;
            this.humidity = humidity;
            this.vibration = vibration;
            this.source = source;
            this.status = status;
        }

        public Long getId() {
            return id;
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

        public String getSource() {
            return source;
        }

        public String getStatus() {
            return status;
        }
    }

    // =====================================================
    // REPEATABILITY REPORT
    // =====================================================

    public static class RepeatabilityReport {

        private Long id;
        private Long testRunId;
        private Double referenceWeight;
        private Double observedWeight;
        private Integer readingNumber;
        private LocalDateTime createdAt;

        public RepeatabilityReport(
                Long id,
                Long testRunId,
                Double referenceWeight,
                Double observedWeight,
                Integer readingNumber,
                LocalDateTime createdAt
        ) {
            this.id = id;
            this.testRunId = testRunId;
            this.referenceWeight = referenceWeight;
            this.observedWeight = observedWeight;
            this.readingNumber = readingNumber;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public Long getTestRunId() {
            return testRunId;
        }

        public Double getReferenceWeight() {
            return referenceWeight;
        }

        public Double getObservedWeight() {
            return observedWeight;
        }

        public Integer getReadingNumber() {
            return readingNumber;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    // =====================================================
    // ECCENTRICITY REPORT
    // =====================================================

    public static class EccentricityReport {

        private Long id;
        private String position;
        private Double referenceWeight;
        private Double observedWeight;
        private LocalDateTime createdAt;

        public EccentricityReport(
                Long id,
                String position,
                Double referenceWeight,
                Double observedWeight,
                LocalDateTime createdAt
        ) {
            this.id = id;
            this.position = position;
            this.referenceWeight = referenceWeight;
            this.observedWeight = observedWeight;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public String getPosition() {
            return position;
        }

        public Double getReferenceWeight() {
            return referenceWeight;
        }

        public Double getObservedWeight() {
            return observedWeight;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}