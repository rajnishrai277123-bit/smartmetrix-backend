package com.smartmetrix.backend.ml;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MlPredictionService {

    private final TestRecordRepository testRecordRepository;
    private final InspectionRepository inspectionRepository;

    public MlPredictionService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
    }

    public MlPredictionResponse predict(Long instrumentId) {

        if (instrumentId == null) {
            throw new IllegalArgumentException(
                    "Instrument ID is required");
        }

        /*
         * Only valid WEIGHING_PERFORMANCE records
         * belonging to this instrument are used.
         */
        List<TestRecord> records =
                findInstrumentRecords(instrumentId);

        /*
         * Sort oldest -> newest.
         */
        records.sort(
                Comparator.comparing(
                        TestRecord::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()))
        );

        /*
         * Build historical prediction points.
         */
        List<PredictionPoint> history =
                new ArrayList<>();

        double totalError = 0.0;

        for (TestRecord record : records) {

            if (record.getReferenceWeight() == null ||
                    record.getObservedWeight() == null) {
                continue;
            }

            double error;

            if (record.getError() != null) {

                error = record.getError();

            } else {

                error =
                        record.getObservedWeight()
                                - record.getReferenceWeight();
            }

            history.add(
                    new PredictionPoint(
                            record.getId(),
                            record.getReferenceWeight(),
                            record.getObservedWeight(),
                            error
                    )
            );

            totalError += error;
        }

        int validRecords = history.size();

        /*
         * If fewer than 3 historical WP records exist,
         * do not generate a fake prediction.
         *
         * We still return the actual historical data
         * so the frontend can display it.
         */
        if (validRecords < 3) {

            double averageError =
                    validRecords == 0
                            ? 0.0
                            : totalError / validRecords;

            return new MlPredictionResponse(
                    instrumentId,
                    "INSUFFICIENT_DATA",
                    round(averageError),
                    0.0,
                    0.0,
                    0.0,
                    "UNKNOWN",
                    "At least three valid weighing-performance records are required for prediction.",
                    history
            );
        }

        /*
         * -----------------------------------------
         * Average historical error
         * -----------------------------------------
         */

        double averageError =
                totalError / validRecords;

        /*
         * -----------------------------------------
         * Linear regression
         * -----------------------------------------
         *
         * X = measurement sequence
         * Y = measurement error
         *
         * Example:
         *
         * Record 1 -> X = 1
         * Record 2 -> X = 2
         * Record 3 -> X = 3
         */

        int n = history.size();

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {

            double x = i + 1;

            double y =
                    history.get(i).getError();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator =
                n * sumX2
                        - sumX * sumX;

        double slope = 0.0;
        double intercept = 0.0;

        if (denominator != 0.0) {

            slope =
                    (n * sumXY
                            - sumX * sumY)
                            / denominator;

            intercept =
                    (sumY
                            - slope * sumX)
                            / n;
        }

        /*
         * -----------------------------------------
         * Predict next measurement error
         * -----------------------------------------
         */

        double nextX = n + 1;

        double predictedNextError =
                intercept
                        + slope * nextX;

        /*
         * -----------------------------------------
         * Confidence indicator
         * -----------------------------------------
         */

        double confidence =
                calculateConfidence(n);

        /*
         * -----------------------------------------
         * Predicted risk
         * -----------------------------------------
         */

        String predictedRisk =
                determineRisk(
                        predictedNextError,
                        slope
                );

        /*
         * -----------------------------------------
         * Explanation
         * -----------------------------------------
         */

        String explanation =
                generateExplanation(
                        predictedNextError,
                        slope,
                        predictedRisk
                );

        /*
         * -----------------------------------------
         * Final response
         * -----------------------------------------
         */

        return new MlPredictionResponse(
                instrumentId,
                "PREDICTION_AVAILABLE",
                round(averageError),
                round(predictedNextError),
                round(slope),
                round(confidence),
                predictedRisk,
                explanation,
                history
        );
    }

    // =================================================
    // FIND INSTRUMENT WP RECORDS
    // =================================================

    private List<TestRecord> findInstrumentRecords(
            Long instrumentId) {

        List<TestRecord> allRecords =
                testRecordRepository.findAll();

        List<TestRecord> result =
                new ArrayList<>();

        for (TestRecord record : allRecords) {

            /*
             * Inspection relationship required.
             */
            if (record.getInspectionId() == null) {
                continue;
            }

            /*
             * ML uses ONLY weighing-performance records.
             */
            if (!"WEIGHING_PERFORMANCE".equals(
                    record.getTestType())) {
                continue;
            }

            /*
             * Reference and observed values are
             * required for historical prediction.
             */
            if (record.getReferenceWeight() == null ||
                    record.getObservedWeight() == null) {
                continue;
            }

            Inspection inspection =
                    inspectionRepository
                            .findById(
                                    record.getInspectionId()
                            )
                            .orElse(null);

            if (inspection == null) {
                continue;
            }

            /*
             * Stable relationship:
             *
             * TestRecord
             *      ↓ inspectionId
             * Inspection
             *      ↓ instrumentId
             * Instrument
             */
            if (instrumentId.equals(
                    inspection.getInstrumentId())) {

                result.add(record);
            }
        }

        return result;
    }

    // =================================================
    // PREDICTED RISK
    // =================================================

    private String determineRisk(
            double predictedError,
            double slope) {

        double absolutePrediction =
                Math.abs(predictedError);

        /*
         * Prototype research thresholds.
         *
         * These are NOT OIML MPE limits.
         */

        if (absolutePrediction >= 0.05) {
            return "HIGH";
        }

        if (absolutePrediction >= 0.02) {
            return "MEDIUM";
        }

        /*
         * Strong error trend.
         */
        if (Math.abs(slope) >= 0.01) {
            return "MEDIUM";
        }

        return "LOW";
    }

    // =================================================
    // EXPLANATION
    // =================================================

    private String generateExplanation(
            double predictedError,
            double slope,
            String predictedRisk) {

        double absolutePrediction =
                Math.abs(predictedError);

        if ("HIGH".equals(predictedRisk)) {

            if (slope > 0) {

                return "The predicted next measurement error is relatively high and the historical error trend is increasing. Additional inspection is recommended.";

            } else if (slope < 0) {

                return "The predicted next measurement error is relatively high despite a decreasing error trend. Additional monitoring is recommended.";

            } else {

                return "The predicted next measurement error is relatively high. Additional inspection is recommended.";
            }
        }

        if ("MEDIUM".equals(predictedRisk)) {

            if (slope > 0) {

                return "The predicted measurement error is moderate and shows an increasing trend. Increase monitoring.";

            } else if (slope < 0) {

                return "The predicted measurement error is moderate but the trend is decreasing. Continue monitoring.";

            } else {

                return "The predicted measurement error is moderate. Continue monitoring future measurements.";
            }
        }

        if (absolutePrediction < 0.01) {

            return "The predicted next measurement error is low and the historical error trend is stable.";
        }

        return "The predicted measurement error is currently low. Continue routine monitoring.";
    }

    // =================================================
    // CONFIDENCE
    // =================================================

    private double calculateConfidence(int count) {

        /*
         * Prototype confidence indicator.
         *
         * This is NOT:
         * - statistical confidence
         * - measurement uncertainty
         * - regulatory confidence
         */

        return Math.min(
                95,
                50 + count * 5
        );
    }

    // =================================================
    // ROUND
    // =================================================

    private double round(double value) {

        return Math.round(
                value * 100000.0
        ) / 100000.0;
    }
}