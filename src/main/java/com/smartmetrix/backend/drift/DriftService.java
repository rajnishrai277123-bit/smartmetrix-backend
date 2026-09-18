package com.smartmetrix.backend.drift;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DriftService {

    private final TestRecordRepository testRecordRepository;
    private final InspectionRepository inspectionRepository;

    public DriftService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
    }

    public DriftAnalysisResponse analyzeInstrument(Long instrumentId) {

        if (instrumentId == null) {
            throw new IllegalArgumentException(
                    "Instrument ID is required");
        }

        // -----------------------------------------
        // Get all test records
        // -----------------------------------------

        List<TestRecord> allRecords =
                testRecordRepository.findAll();

        List<TestRecord> instrumentRecords =
                new ArrayList<>();

        // -----------------------------------------
        // Find records belonging to this instrument
        // -----------------------------------------

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

                // Only weighing-performance records
                if ("WEIGHING_PERFORMANCE".equals(
                        record.getTestType())) {

                    instrumentRecords.add(record);
                }
            }
        }

        // -----------------------------------------
        // Sort by measurement date/time
        // Oldest -> Newest
        // -----------------------------------------

        instrumentRecords.sort(
                Comparator.comparing(
                        TestRecord::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()))
        );

        // -----------------------------------------
        // No historical data
        // -----------------------------------------

        if (instrumentRecords.isEmpty()) {

            return new DriftAnalysisResponse(
                    instrumentId,
                    0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "INSUFFICIENT_DATA",
                    "Not enough historical weighing records for drift analysis.",
                    new ArrayList<>()
            );
        }

        // -----------------------------------------
        // Calculate statistics
        // -----------------------------------------

        double totalError = 0.0;

        double totalAbsoluteError = 0.0;

        double maximumAbsoluteError = 0.0;

        List<DriftPoint> history =
                new ArrayList<>();

        for (TestRecord record : instrumentRecords) {

            Double error =
                    record.getError();

            if (error == null) {
                continue;
            }

            totalError += error;

            totalAbsoluteError +=
                    Math.abs(error);

            maximumAbsoluteError =
                    Math.max(
                            maximumAbsoluteError,
                            Math.abs(error)
                    );

            history.add(
                    new DriftPoint(
                            record.getId(),
                            record.getInspectionId(),
                            record.getReferenceWeight(),
                            record.getObservedWeight(),
                            record.getError(),
                            record.getResult()
                    )
            );
        }

        int validRecords =
                history.size();

        // -----------------------------------------
        // No valid error data
        // -----------------------------------------

        if (validRecords == 0) {

            return new DriftAnalysisResponse(
                    instrumentId,
                    0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "INSUFFICIENT_DATA",
                    "Historical records exist, but no valid error values are available.",
                    history
            );
        }

        // -----------------------------------------
        // Average error
        // -----------------------------------------

        double averageError =
                totalError / validRecords;

        // -----------------------------------------
        // Average absolute error
        // -----------------------------------------

        double averageAbsoluteError =
                totalAbsoluteError / validRecords;

        // -----------------------------------------
        // Calculate drift score
        // -----------------------------------------

        double driftScore =
                calculateDriftScore(
                        history,
                        averageAbsoluteError
                );

        // -----------------------------------------
        // Determine status
        // -----------------------------------------

        String driftStatus;

        String explanation;

        if (validRecords < 3) {

            driftStatus =
                    "INSUFFICIENT_DATA";

            explanation =
                    "At least three valid historical readings are recommended for trend-based drift analysis.";

        } else if (driftScore < 30) {

            driftStatus =
                    "STABLE";

            explanation =
                    "Historical measurement error is relatively stable.";

        } else if (driftScore < 60) {

            driftStatus =
                    "WARNING";

            explanation =
                    "Historical data indicates a moderate change in measurement error.";

        } else {

            driftStatus =
                    "HIGH_DRIFT";

            explanation =
                    "Historical data indicates a significant change in measurement error. Further inspection is recommended.";
        }

        // -----------------------------------------
        // Round values
        // -----------------------------------------

        averageError =
                round(averageError);

        averageAbsoluteError =
                round(averageAbsoluteError);

        maximumAbsoluteError =
                round(maximumAbsoluteError);

        driftScore =
                round(driftScore);

        return new DriftAnalysisResponse(
                instrumentId,
                validRecords,
                averageError,
                averageAbsoluteError,
                maximumAbsoluteError,
                driftScore,
                driftStatus,
                explanation,
                history
        );
    }

    // =================================================
    // DRIFT SCORE
    // =================================================

    private double calculateDriftScore(
            List<DriftPoint> history,
            double averageAbsoluteError) {

        if (history.size() < 2) {
            return 0.0;
        }

        // -----------------------------------------
        // Split history into early and later data
        // -----------------------------------------

        int split =
                history.size() / 2;

        if (split == 0) {
            return 0.0;
        }

        double earlyAverage = 0.0;

        double laterAverage = 0.0;

        // -----------------------------------------
        // Early measurements
        // -----------------------------------------

        for (int i = 0; i < split; i++) {

            earlyAverage +=
                    Math.abs(
                            history.get(i).getError()
                    );
        }

        earlyAverage =
                earlyAverage / split;

        // -----------------------------------------
        // Later measurements
        // -----------------------------------------

        for (int i = split;
             i < history.size();
             i++) {

            laterAverage +=
                    Math.abs(
                            history.get(i).getError()
                    );
        }

        laterAverage =
                laterAverage /
                        (history.size() - split);

        // -----------------------------------------
        // Calculate percentage change
        // -----------------------------------------

        if (earlyAverage == 0.0) {

            if (laterAverage == 0.0) {
                return 0.0;
            }

            return 100.0;
        }

        double change =
                Math.abs(
                        laterAverage - earlyAverage
                )
                        / earlyAverage
                        * 100.0;

        // -----------------------------------------
        // Convert to 0-100 score
        // -----------------------------------------

        double score =
                Math.min(change, 100.0);

        // -----------------------------------------
        // Add average error stability factor
        // -----------------------------------------

        if (averageAbsoluteError > 0) {

            score =
                    Math.min(
                            100.0,
                            score * 0.8
                                    + Math.min(
                                    averageAbsoluteError * 1000,
                                    20.0)
                    );
        }

        return score;
    }

    // =================================================
    // ROUND
    // =================================================

    private double round(double value) {

        return Math.round(
                value * 10000.0
        ) / 10000.0;
    }
}