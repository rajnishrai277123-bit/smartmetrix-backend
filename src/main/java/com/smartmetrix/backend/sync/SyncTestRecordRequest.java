package com.smartmetrix.backend.sync;

import java.util.List;

public class SyncTestRecordRequest {

    private List<OfflineTestRecord> records;

    public SyncTestRecordRequest() {
    }

    public List<OfflineTestRecord> getRecords() {
        return records;
    }

    public void setRecords(List<OfflineTestRecord> records) {
        this.records = records;
    }

    public static class OfflineTestRecord {

        private String clientRecordId;
        private Long inspectionId;
        private String testType;
        private Double referenceWeight;
        private Double observedWeight;
        private Double temperature;
        private Double humidity;
        private Double vibration;
        private String testStage;

        public OfflineTestRecord() {
        }

        public String getClientRecordId() {
            return clientRecordId;
        }

        public void setClientRecordId(String clientRecordId) {
            this.clientRecordId = clientRecordId;
        }

        public Long getInspectionId() {
            return inspectionId;
        }

        public void setInspectionId(Long inspectionId) {
            this.inspectionId = inspectionId;
        }

        public String getTestType() {
            return testType;
        }

        public void setTestType(String testType) {
            this.testType = testType;
        }

        public Double getReferenceWeight() {
            return referenceWeight;
        }

        public void setReferenceWeight(Double referenceWeight) {
            this.referenceWeight = referenceWeight;
        }

        public Double getObservedWeight() {
            return observedWeight;
        }

        public void setObservedWeight(Double observedWeight) {
            this.observedWeight = observedWeight;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getHumidity() {
            return humidity;
        }

        public void setHumidity(Double humidity) {
            this.humidity = humidity;
        }

        public Double getVibration() {
            return vibration;
        }

        public void setVibration(Double vibration) {
            this.vibration = vibration;
        }

        public String getTestStage() {
            return testStage;
        }

        public void setTestStage(String testStage) {
            this.testStage = testStage;
        }
    }
}