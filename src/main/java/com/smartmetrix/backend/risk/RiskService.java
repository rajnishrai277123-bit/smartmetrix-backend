package com.smartmetrix.backend.risk;

import com.smartmetrix.backend.drift.DriftAnalysisResponse;
import com.smartmetrix.backend.drift.DriftService;
import com.smartmetrix.backend.environment.EnvironmentRecord;
import com.smartmetrix.backend.environment.EnvironmentRecordRepository;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskService {

    private final DriftService driftService;
    private final TestRecordRepository testRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final EnvironmentRecordRepository environmentRecordRepository;

    public RiskService(
            DriftService driftService,
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository,
            EnvironmentRecordRepository environmentRecordRepository) {

        this.driftService = driftService;
        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.environmentRecordRepository = environmentRecordRepository;
    }

    public RiskResponse predictRisk(Long instrumentId) {

        if (instrumentId == null) {
            throw new IllegalArgumentException(
                    "Instrument ID is required");
        }

        // -----------------------------------------
        // Get drift analysis
        // -----------------------------------------

        DriftAnalysisResponse drift =
                driftService.analyzeInstrument(instrumentId);

        double driftScore =
                drift.getDriftScore();

        // -----------------------------------------
        // Find test records for instrument
        // -----------------------------------------

        List<TestRecord> allRecords =
                testRecordRepository.findAll();

        List<TestRecord> instrumentRecords =
                new ArrayList<>();

        for (TestRecord record : allRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            Inspection inspection =
                    inspectionRepository
                            .findById(record.getInspectionId())
                            .orElse(null);

            if (inspection == null) {
                continue;
            }

            if (instrumentId.equals(
                    inspection.getInstrumentId())) {

                instrumentRecords.add(record);
            }
        }

        // -----------------------------------------
        // Count failures
        // -----------------------------------------

        long totalTestRecords =
                instrumentRecords.stream()
                        .filter(record ->
                                record.getResult() != null)
                        .count();

        long failTestRecords =
                instrumentRecords.stream()
                        .filter(record ->
                                "FAIL".equals(
                                        record.getResult()))
                        .count();

        // -----------------------------------------
        // Environment data
        // -----------------------------------------

        String environmentStatus =
                getEnvironmentStatus(
                        instrumentRecords
                );

        Double temperature =
                getLatestTemperature(
                        instrumentRecords
                );

        Double humidity =
                getLatestHumidity(
                        instrumentRecords
                );

        Double vibration =
                getLatestVibration(
                        instrumentRecords
                );

        // -----------------------------------------
        // Calculate risk score
        // -----------------------------------------

        int riskScore = 0;

        List<String> reasons =
                new ArrayList<>();

        // Drift contribution: maximum 50 points
        if (driftScore >= 60) {

            riskScore += 50;

            reasons.add(
                    "High measurement drift detected");

        } else if (driftScore >= 30) {

            riskScore += 30;

            reasons.add(
                    "Moderate measurement drift detected");

        } else if (driftScore > 0) {

            riskScore += 10;
        }

        // -----------------------------------------
        // Test failure contribution
        // -----------------------------------------

        if (failTestRecords >= 3) {

            riskScore += 30;

            reasons.add(
                    "Multiple historical test failures detected");

        } else if (failTestRecords >= 1) {

            riskScore += 20;

            reasons.add(
                    "Historical test failure detected");
        }

        // -----------------------------------------
        // Environment contribution
        // -----------------------------------------

        if ("OUT_OF_RANGE".equals(
                environmentStatus)) {

            riskScore += 20;

            reasons.add(
                    "Environmental conditions are out of prototype assessment range");

        } else if ("WARNING".equals(
                environmentStatus)) {

            riskScore += 10;

            reasons.add(
                    "Environmental conditions require attention");
        }

        // -----------------------------------------
        // Limit score
        // -----------------------------------------

        riskScore =
                Math.min(riskScore, 100);

        // -----------------------------------------
        // Determine risk level
        // -----------------------------------------

        String riskLevel;

        String recommendation;

        if (totalTestRecords == 0) {

            riskLevel =
                    "INSUFFICIENT_DATA";

            recommendation =
                    "Perform additional weighing tests before assigning an instrument risk level.";

            reasons.add(
                    "No valid historical test records are available");

        } else if (riskScore < 30) {

            riskLevel =
                    "LOW";

            recommendation =
                    "Continue routine inspection and monitoring.";

        } else if (riskScore < 60) {

            riskLevel =
                    "MEDIUM";

            recommendation =
                    "Increase monitoring and consider an additional inspection.";

        } else {

            riskLevel =
                    "HIGH";

            recommendation =
                    "Schedule an additional inspection and investigate possible measurement drift.";
        }

        // -----------------------------------------
        // If no reason exists
        // -----------------------------------------

        if (reasons.isEmpty()) {

            reasons.add(
                    "No significant risk indicators detected");
        }

        // -----------------------------------------
        // Return response
        // -----------------------------------------

        return new RiskResponse(
                instrumentId,
                riskScore,
                riskLevel,
                driftScore,
                totalTestRecords,
                failTestRecords,
                environmentStatus,
                temperature,
                humidity,
                vibration,
                reasons,
                recommendation
        );
    }

    // -----------------------------------------
    // Environment status
    // -----------------------------------------

    private String getEnvironmentStatus(
            List<TestRecord> instrumentRecords) {

        if (instrumentRecords.isEmpty()) {
            return "UNKNOWN";
        }

        String finalStatus = "UNKNOWN";

        for (TestRecord testRecord : instrumentRecords) {

            if (testRecord.getInspectionId() == null) {
                continue;
            }

            List<EnvironmentRecord> records =
                    environmentRecordRepository
                            .findByInspectionId(
                                    testRecord.getInspectionId()
                            );

            for (EnvironmentRecord record : records) {

                String status =
                        record.getStatus();

                if ("OUT_OF_RANGE".equals(status)) {
                    return "OUT_OF_RANGE";
                }

                if ("WARNING".equals(status)) {
                    finalStatus = "WARNING";
                }

                if ("NORMAL".equals(status)
                        && "UNKNOWN".equals(finalStatus)) {

                    finalStatus = "NORMAL";
                }
            }
        }

        return finalStatus;
    }

    // -----------------------------------------
    // Latest temperature
    // -----------------------------------------

    private Double getLatestTemperature(
            List<TestRecord> instrumentRecords) {

        Double latestTemperature = null;

        for (TestRecord testRecord : instrumentRecords) {

            if (testRecord.getInspectionId() == null) {
                continue;
            }

            List<EnvironmentRecord> records =
                    environmentRecordRepository
                            .findByInspectionId(
                                    testRecord.getInspectionId()
                            );

            for (EnvironmentRecord record : records) {

                if (record.getTemperature() != null) {
                    latestTemperature =
                            record.getTemperature();
                }
            }
        }

        return latestTemperature;
    }

    // -----------------------------------------
    // Latest humidity
    // -----------------------------------------

    private Double getLatestHumidity(
            List<TestRecord> instrumentRecords) {

        Double latestHumidity = null;

        for (TestRecord testRecord : instrumentRecords) {

            if (testRecord.getInspectionId() == null) {
                continue;
            }

            List<EnvironmentRecord> records =
                    environmentRecordRepository
                            .findByInspectionId(
                                    testRecord.getInspectionId()
                            );

            for (EnvironmentRecord record : records) {

                if (record.getHumidity() != null) {
                    latestHumidity =
                            record.getHumidity();
                }
            }
        }

        return latestHumidity;
    }

    // -----------------------------------------
    // Latest vibration
    // -----------------------------------------

    private Double getLatestVibration(
            List<TestRecord> instrumentRecords) {

        Double latestVibration = null;

        for (TestRecord testRecord : instrumentRecords) {

            if (testRecord.getInspectionId() == null) {
                continue;
            }

            List<EnvironmentRecord> records =
                    environmentRecordRepository
                            .findByInspectionId(
                                    testRecord.getInspectionId()
                            );

            for (EnvironmentRecord record : records) {

                if (record.getVibration() != null) {
                    latestVibration =
                            record.getVibration();
                }
            }
        }

        return latestVibration;
    }
}