
package com.smartmetrix.backend.analytics;

import com.smartmetrix.backend.environment.EnvironmentRecord;
import com.smartmetrix.backend.environment.EnvironmentRecordRepository;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.test.EccentricityRecord;
import com.smartmetrix.backend.test.EccentricityRecordRepository;
import com.smartmetrix.backend.test.EccentricityRecordService;
import com.smartmetrix.backend.test.EccentricitySummaryResponse;
import com.smartmetrix.backend.test.RepeatabilityRecord;
import com.smartmetrix.backend.test.RepeatabilityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecordService;
import com.smartmetrix.backend.test.RepeatabilitySummaryResponse;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class AdvancedAnalyticsService {

    private final TestRecordRepository testRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final EnvironmentRecordRepository environmentRecordRepository;
    private final InstrumentRepository instrumentRepository;

    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final EccentricityRecordRepository eccentricityRecordRepository;

    private final RepeatabilityRecordService repeatabilityRecordService;
    private final EccentricityRecordService eccentricityRecordService;

    public AdvancedAnalyticsService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository,
            EnvironmentRecordRepository environmentRecordRepository,
            InstrumentRepository instrumentRepository,
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            EccentricityRecordRepository eccentricityRecordRepository,
            RepeatabilityRecordService repeatabilityRecordService,
            EccentricityRecordService eccentricityRecordService) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.environmentRecordRepository = environmentRecordRepository;
        this.instrumentRepository = instrumentRepository;

        this.repeatabilityRecordRepository = repeatabilityRecordRepository;
        this.eccentricityRecordRepository = eccentricityRecordRepository;

        this.repeatabilityRecordService = repeatabilityRecordService;
        this.eccentricityRecordService = eccentricityRecordService;
    }

    public AdvancedAnalyticsResponse analyzeInstrument(Long instrumentId) {

        // =====================================================
        // 0. VALIDATE INSTRUMENT ID
        // =====================================================

        if (instrumentId == null || instrumentId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid positive instrument ID is required"
            );
        }

        /*
         * Verify that the instrument exists before calculating
         * analytics. A nonexistent instrument must not receive
         * a normal analytics response with zero values.
         */

        instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrument not found with ID: " + instrumentId
                ));

        // =====================================================
        // 1. WEIGHING PERFORMANCE RECORDS
        // =====================================================

        List<TestRecord> allRecords = testRecordRepository.findAll();

        List<TestRecord> records = new ArrayList<>();

        /*
         * Only completed PASS/FAIL WEIGHING_PERFORMANCE records
         * with a recorded measurement error are included.
         *
         * Records with null error are excluded from the valid
         * analytics set because null must not be interpreted as
         * a genuine zero measurement error.
         *
         * Repeatability and Eccentricity use separate metrics.
         */

        for (TestRecord record : allRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            if (!"WEIGHING_PERFORMANCE".equals(record.getTestType())) {
                continue;
            }

            if (!isFinalResult(record.getResult())) {
                continue;
            }

            if (record.getError() == null) {
                continue;
            }

            Inspection inspection = inspectionRepository
                    .findById(record.getInspectionId())
                    .orElse(null);

            if (inspection == null) {
                continue;
            }

            if (instrumentId.equals(inspection.getInstrumentId())) {
                records.add(record);
            }
        }

        // =====================================================
        // 2. REPEATABILITY TESTS
        // =====================================================

        List<RepeatabilityRecord> allRepeatabilityRecords =
                repeatabilityRecordRepository.findAll();

        /*
         * One testRunId represents ONE repeatability test.
         *
         * A valid run must contain exactly five readings:
         * 1, 2, 3, 4 and 5.
         *
         * Duplicate or missing reading numbers are rejected.
         * All readings must belong to the same inspection.
         */

        Set<Long> repeatabilityTestRunIds = new HashSet<>();

        for (RepeatabilityRecord record : allRepeatabilityRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            if (record.getTestRunId() == null) {
                continue;
            }

            Inspection inspection = inspectionRepository
                    .findById(record.getInspectionId())
                    .orElse(null);

            if (inspection == null) {
                continue;
            }

            if (!instrumentId.equals(inspection.getInstrumentId())) {
                continue;
            }

            Long testRunId = record.getTestRunId();

            // Avoid validating the same run repeatedly.
            if (repeatabilityTestRunIds.contains(testRunId)) {
                continue;
            }

            List<RepeatabilityRecord> runRecords =
                    repeatabilityRecordRepository.findByTestRunId(testRunId);

            if (!isValidRepeatabilityRun(
                    runRecords,
                    record.getInspectionId())) {
                continue;
            }

            /*
             * Validate the run using the existing summary service.
             * Invalid historical runs must not affect analytics.
             */

            try {

                RepeatabilitySummaryResponse summary =
                        repeatabilityRecordService.getSummary(testRunId);

                if (summary != null
                        && isFinalResult(summary.getResult())) {

                    repeatabilityTestRunIds.add(testRunId);
                }

            } catch (Exception ignored) {

                // Invalid historical runs are excluded.
            }
        }

        // =====================================================
        // 3. ECCENTRICITY TESTS
        // =====================================================

        List<EccentricityRecord> allEccentricityRecords =
                eccentricityRecordRepository.findAll();

        /*
         * One inspection represents ONE eccentricity test.
         *
         * A valid test requires all five unique positions:
         * LEFT, RIGHT, FRONT, BACK and CENTER.
         *
         * Historical REAR positions are normalized to BACK.
         * Missing or duplicate positions are rejected.
         */

        Set<Long> eccentricityInspectionIds = new HashSet<>();

        for (EccentricityRecord record : allEccentricityRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            Inspection inspection = inspectionRepository
                    .findById(record.getInspectionId())
                    .orElse(null);

            if (inspection == null) {
                continue;
            }

            if (!instrumentId.equals(inspection.getInstrumentId())) {
                continue;
            }

            Long inspectionId = record.getInspectionId();

            // Avoid validating the same inspection repeatedly.
            if (eccentricityInspectionIds.contains(inspectionId)) {
                continue;
            }

            List<EccentricityRecord> inspectionRecords =
                    eccentricityRecordRepository.findByInspectionId(
                            inspectionId
                    );

            if (!isValidEccentricityTest(
                    inspectionRecords,
                    inspectionId)) {
                continue;
            }

            /*
             * Use the existing service to calculate the actual result.
             * Only completed PASS/FAIL tests are counted.
             */

            try {

                EccentricitySummaryResponse summary =
                        eccentricityRecordService.getSummary(inspectionId);

                if (summary != null
                        && isFinalResult(summary.getResult())) {

                    eccentricityInspectionIds.add(inspectionId);
                }

            } catch (Exception ignored) {

                // Invalid historical eccentricity tests are excluded.
            }
        }

        // =====================================================
        // 4. CHECK WHETHER ANY VALID ANALYTICS DATA EXISTS
        // =====================================================

        if (records.isEmpty()
                && repeatabilityTestRunIds.isEmpty()
                && eccentricityInspectionIds.isEmpty()) {

            return new AdvancedAnalyticsResponse(
                    instrumentId,
                    0,
                    "INSUFFICIENT_DATA",
                    0,
                    "INSUFFICIENT_DATA",
                    0,
                    0,
                    0,
                    0,
                    "INSUFFICIENT_DATA",
                    "Perform more valid weighing, repeatability or eccentricity tests before prediction.",
                    new ArrayList<>()
            );
        }

        // =====================================================
        // 5. TOTAL TEST COUNT
        // =====================================================

        long weighingPerformanceTests = records.size();

        long repeatabilityTests = repeatabilityTestRunIds.size();

        long eccentricityTests = eccentricityInspectionIds.size();

        /*
         * Count actual test executions, not individual readings.
         *
         * WP              = 1 TestRecord = 1 test
         * Repeatability   = 1 testRunId = 1 test
         * Eccentricity    = 1 inspection = 1 test
         */

        long totalTests = weighingPerformanceTests
                + repeatabilityTests
                + eccentricityTests;

        // =====================================================
        // 6. FAILED TESTS
        // =====================================================

        /*
         * Start with failed Weighing Performance tests.
         */

        long failedTests = records.stream()
                .filter(r -> "FAIL".equals(r.getResult()))
                .count();

        // -----------------------------------------------------
        // REPEATABILITY FAILED RUNS
        // -----------------------------------------------------

        for (Long testRunId : repeatabilityTestRunIds) {

            try {

                RepeatabilitySummaryResponse summary =
                        repeatabilityRecordService.getSummary(testRunId);

                if (summary != null
                        && "FAIL".equals(summary.getResult())) {

                    failedTests++;
                }

            } catch (Exception ignored) {

                // Invalid historical repeatability runs are excluded.
            }
        }

        // -----------------------------------------------------
        // ECCENTRICITY FAILED TESTS
        // -----------------------------------------------------

        for (Long inspectionId : eccentricityInspectionIds) {

            try {

                EccentricitySummaryResponse summary =
                        eccentricityRecordService.getSummary(inspectionId);

                if (summary != null
                        && "FAIL".equals(summary.getResult())) {

                    failedTests++;
                }

            } catch (Exception ignored) {

                // Invalid historical eccentricity tests are excluded.
            }
        }

        // =====================================================
        // 7. WEIGHING PERFORMANCE ERROR STATISTICS
        // =====================================================

        /*
         * Error statistics remain WP-only.
         *
         * Do not combine:
         * - WP measurement error
         * - Repeatability range
         * - Eccentricity maximum difference
         *
         * Null errors have already been excluded from records.
         */

        double averageError = records.stream()
                .mapToDouble(r -> Math.abs(r.getError()))
                .average()
                .orElse(0.0);

        double maximumAbsoluteError = records.stream()
                .mapToDouble(r -> Math.abs(r.getError()))
                .max()
                .orElse(0.0);

        // =====================================================
        // 8. INSTRUMENT HEALTH SCORE
        // =====================================================

        double healthScore = 100.0;

        /*
         * Custom project scoring rule:
         * Every failed test execution gives a penalty of 10 points.
         */

        healthScore -= failedTests * 10.0;

        /*
         * Apply a penalty based on average WP absolute error.
         * The maximum average-error penalty is 30 points.
         */

        healthScore -= Math.min(
                30.0,
                averageError * 100.0
        );

        /*
         * Apply a penalty based on maximum WP absolute error.
         * The maximum error penalty is 20 points.
         */

        healthScore -= Math.min(
                20.0,
                maximumAbsoluteError * 100.0
        );

        // Keep the health score within 0 to 100.
        healthScore = Math.max(
                0.0,
                Math.min(100.0, healthScore)
        );

        // Round the health score to two decimal places.
        healthScore = Math.round(
                healthScore * 100.0
        ) / 100.0;

        String healthStatus;

        if (totalTests < 3) {

            healthStatus = "INSUFFICIENT_DATA";

        } else if (healthScore >= 80.0) {

            healthStatus = "HEALTHY";

        } else if (healthScore >= 60.0) {

            healthStatus = "WARNING";

        } else {

            healthStatus = "CRITICAL";
        }

        // =====================================================
        // 9. ENVIRONMENT IMPACT
        // =====================================================

        /*
         * Environment data belongs to an inspection.
         *
         * Collect unique inspection IDs associated with valid:
         * - Weighing Performance tests
         * - Repeatability tests
         * - Eccentricity tests
         */

        Set<Long> analyticsInspectionIds = new HashSet<>();

        // -----------------------------------------------------
        // WP inspections
        // -----------------------------------------------------

        for (TestRecord record : records) {

            if (record.getInspectionId() != null) {

                analyticsInspectionIds.add(
                        record.getInspectionId()
                );
            }
        }

        // -----------------------------------------------------
        // Repeatability inspections
        // -----------------------------------------------------

        for (Long testRunId : repeatabilityTestRunIds) {

            List<RepeatabilityRecord> runRecords =
                    repeatabilityRecordRepository.findByTestRunId(
                            testRunId
                    );

            if (runRecords != null && !runRecords.isEmpty()) {

                Long inspectionId = runRecords.get(0).getInspectionId();

                if (inspectionId != null) {
                    analyticsInspectionIds.add(inspectionId);
                }
            }
        }

        // -----------------------------------------------------
        // Eccentricity inspections
        // -----------------------------------------------------

        for (Long inspectionId : eccentricityInspectionIds) {

            analyticsInspectionIds.add(inspectionId);
        }

        double environmentImpactScore = 0.0;

        int environmentSamples = 0;

        for (Long inspectionId : analyticsInspectionIds) {

            List<EnvironmentRecord> environmentRecords =
                    environmentRecordRepository.findByInspectionId(
                            inspectionId
                    );

            if (environmentRecords == null) {
                continue;
            }

            for (EnvironmentRecord environmentRecord : environmentRecords) {

                if (environmentRecord == null) {
                    continue;
                }

                String status = environmentRecord.getStatus();

                /*
                 * Custom project environment scoring:
                 *
                 * NORMAL       = 0 penalty
                 * WARNING      = 10 penalty
                 * OUT_OF_RANGE = 20 penalty
                 *
                 * Unknown or null statuses are ignored.
                 */

                if ("OUT_OF_RANGE".equals(status)) {

                    environmentImpactScore += 20.0;
                    environmentSamples++;

                } else if ("WARNING".equals(status)) {

                    environmentImpactScore += 10.0;
                    environmentSamples++;

                } else if ("NORMAL".equals(status)) {

                    environmentImpactScore += 0.0;
                    environmentSamples++;
                }
            }
        }

        // Calculate average environment impact.
        if (environmentSamples > 0) {

            environmentImpactScore =
                    environmentImpactScore / environmentSamples;
        }

        // Keep environment impact between 0 and 100.
        environmentImpactScore = Math.max(
                0.0,
                Math.min(100.0, environmentImpactScore)
        );

        // Round environment impact to two decimal places.
        environmentImpactScore = Math.round(
                environmentImpactScore * 100.0
        ) / 100.0;

        String environmentImpactLevel;

        if (environmentSamples == 0) {

            environmentImpactLevel = "INSUFFICIENT_DATA";

        } else if (environmentImpactScore < 5.0) {

            environmentImpactLevel = "LOW";

        } else if (environmentImpactScore < 15.0) {

            environmentImpactLevel = "MEDIUM";

        } else {

            environmentImpactLevel = "HIGH";
        }

        // =====================================================
        // 10. RECALIBRATION PREDICTION
        // =====================================================

        String recalibrationPrediction;
        String recalibrationRecommendation;

        if (totalTests < 3) {

            recalibrationPrediction = "INSUFFICIENT_DATA";

            recalibrationRecommendation =
                    "Collect at least 3 completed historical tests.";

        } else if (healthScore < 60.0 || failedTests >= 3) {

            recalibrationPrediction = "HIGH_RISK";

            recalibrationRecommendation =
                    "Schedule recalibration or detailed inspection soon.";

        } else if (healthScore < 80.0 || averageError > 0.02) {

            recalibrationPrediction = "MONITOR";

            recalibrationRecommendation =
                    "Increase monitoring and consider recalibration.";

        } else {

            recalibrationPrediction = "LOW_RISK";

            recalibrationRecommendation =
                    "Continue routine inspection schedule.";
        }

        // =====================================================
        // 11. DATE-WISE TREND FOR ALL THREE TEST TYPES
        // =====================================================

        /*
         * Trend includes:
         *
         * WEIGHING_PERFORMANCE:
         *     Absolute WP measurement error.
         *
         * REPEATABILITY:
         *     Range between maximum and minimum observed readings.
         *
         * ECCENTRICITY:
         *     Maximum absolute difference between observed and reference
         *     weight across the five positions.
         *
         * These different metrics are not combined in WP statistics.
         */

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                );

        List<TrendEntry> trendEntries = new ArrayList<>();

        // -----------------------------------------------------
        // WEIGHING PERFORMANCE TREND
        // -----------------------------------------------------

        for (TestRecord record : records) {

            if (record.getCreatedAt() == null) {
                continue;
            }

            double error = Math.abs(record.getError());

            AdvancedAnalyticsResponse.TrendPoint point =
                    new AdvancedAnalyticsResponse.TrendPoint(
                            record.getCreatedAt().format(formatter),
                            round(error),
                            record.getResult(),
                            "WEIGHING_PERFORMANCE"
                    );

            trendEntries.add(
                    new TrendEntry(record.getCreatedAt(), point)
            );
        }

        // -----------------------------------------------------
        // REPEATABILITY TREND
        // -----------------------------------------------------

        for (Long testRunId : repeatabilityTestRunIds) {

            try {

                List<RepeatabilityRecord> runRecords =
                        repeatabilityRecordRepository.findByTestRunId(
                                testRunId
                        );

                if (runRecords == null || runRecords.size() != 5) {
                    continue;
                }

                RepeatabilitySummaryResponse summary =
                        repeatabilityRecordService.getSummary(testRunId);

                if (summary == null
                        || !isFinalResult(summary.getResult())) {
                    continue;
                }

                /*
                 * All five readings must have observed weights.
                 * Otherwise the range cannot be calculated reliably.
                 */

                List<Double> observedWeights = runRecords.stream()
                        .map(RepeatabilityRecord::getObservedWeight)
                        .filter(Objects::nonNull)
                        .toList();

                if (observedWeights.size() != 5) {
                    continue;
                }

                LocalDateTime testDate = runRecords.stream()
                        .map(RepeatabilityRecord::getCreatedAt)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(null);

                if (testDate == null) {
                    continue;
                }

                double minimum = observedWeights.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(0.0);

                double maximum = observedWeights.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0.0);

                double repeatabilityRange = maximum - minimum;

                AdvancedAnalyticsResponse.TrendPoint point =
                        new AdvancedAnalyticsResponse.TrendPoint(
                                testDate.format(formatter),
                                round(repeatabilityRange),
                                summary.getResult(),
                                "REPEATABILITY"
                        );

                trendEntries.add(
                        new TrendEntry(testDate, point)
                );

            } catch (Exception ignored) {

                // Invalid historical runs are excluded from the trend.
            }
        }

        // -----------------------------------------------------
        // ECCENTRICITY TREND
        // -----------------------------------------------------

        for (Long inspectionId : eccentricityInspectionIds) {

            try {

                List<EccentricityRecord> inspectionRecords =
                        eccentricityRecordRepository.findByInspectionId(
                                inspectionId
                        );

                if (inspectionRecords == null
                        || inspectionRecords.size() != 5) {
                    continue;
                }

                EccentricitySummaryResponse summary =
                        eccentricityRecordService.getSummary(inspectionId);

                if (summary == null
                        || !isFinalResult(summary.getResult())) {
                    continue;
                }

                LocalDateTime testDate = inspectionRecords.stream()
                        .map(EccentricityRecord::getCreatedAt)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(null);

                if (testDate == null) {
                    continue;
                }

                /*
                 * Every position must have both reference and observed
                 * weights. All five positions are required.
                 */

                List<Double> positionErrors = inspectionRecords.stream()
                        .filter(r -> r.getReferenceWeight() != null)
                        .filter(r -> r.getObservedWeight() != null)
                        .map(r -> Math.abs(
                                r.getObservedWeight()
                                        - r.getReferenceWeight()
                        ))
                        .toList();

                if (positionErrors.size() != 5) {
                    continue;
                }

                double maximumPositionError = positionErrors.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0.0);

                AdvancedAnalyticsResponse.TrendPoint point =
                        new AdvancedAnalyticsResponse.TrendPoint(
                                testDate.format(formatter),
                                round(maximumPositionError),
                                summary.getResult(),
                                "ECCENTRICITY"
                        );

                trendEntries.add(
                        new TrendEntry(testDate, point)
                );

            } catch (Exception ignored) {

                // Invalid historical eccentricity tests are excluded.
            }
        }

        // Sort all test types by their actual timestamps.
        trendEntries.sort(
                Comparator.comparing(TrendEntry::createdAt)
        );

        List<AdvancedAnalyticsResponse.TrendPoint> trend =
                trendEntries.stream()
                        .map(TrendEntry::point)
                        .toList();

        // =====================================================
        // 12. FINAL RESPONSE
        // =====================================================

        return new AdvancedAnalyticsResponse(
                instrumentId,
                healthScore,
                healthStatus,
                environmentImpactScore,
                environmentImpactLevel,
                round(averageError),
                round(maximumAbsoluteError),
                totalTests,
                failedTests,
                recalibrationPrediction,
                recalibrationRecommendation,
                trend
        );
    }

    // =========================================================
    // HELPER 1: CHECK FINAL TEST RESULT
    // =========================================================

    /*
     * Only PASS and FAIL are completed test results.
     * PENDING and unknown results are not counted.
     */

    private boolean isFinalResult(String result) {

        return "PASS".equals(result)
                || "FAIL".equals(result);
    }

    // =========================================================
    // HELPER 2: VALIDATE REPEATABILITY RUN
    // =========================================================

    /*
     * A valid repeatability run requires:
     * - Exactly five records
     * - Reading numbers 1, 2, 3, 4 and 5
     * - No duplicate reading numbers
     * - All records belong to the expected inspection
     */

    private boolean isValidRepeatabilityRun(
            List<RepeatabilityRecord> runRecords,
            Long expectedInspectionId) {

        if (runRecords == null || runRecords.size() != 5) {
            return false;
        }

        if (expectedInspectionId == null) {
            return false;
        }

        Set<Integer> readingNumbers = new HashSet<>();

        for (RepeatabilityRecord record : runRecords) {

            if (record.getInspectionId() == null) {
                return false;
            }

            if (!expectedInspectionId.equals(
                    record.getInspectionId())) {
                return false;
            }

            Integer readingNumber = record.getReadingNumber();

            if (readingNumber == null) {
                return false;
            }

            if (readingNumber < 1 || readingNumber > 5) {
                return false;
            }

            if (!readingNumbers.add(readingNumber)) {
                return false;
            }

            if (record.getObservedWeight() == null) {
                return false;
            }
        }

        return readingNumbers.size() == 5;
    }

    // =========================================================
    // HELPER 3: VALIDATE ECCENTRICITY TEST
    // =========================================================

    /*
     * A valid eccentricity test requires exactly five records
     * containing all five unique positions:
     *
     * LEFT, RIGHT, FRONT, BACK and CENTER.
     *
     * Historical REAR positions are normalized to BACK.
     * Duplicate, missing or unexpected positions are rejected.
     */

    private boolean isValidEccentricityTest(
            List<EccentricityRecord> inspectionRecords,
            Long expectedInspectionId) {

        if (inspectionRecords == null
                || inspectionRecords.size() != 5) {
            return false;
        }

        if (expectedInspectionId == null) {
            return false;
        }

        Set<String> expectedPositions = Set.of(
                "LEFT",
                "RIGHT",
                "FRONT",
                "BACK",
                "CENTER"
        );

        Set<String> actualPositions = new HashSet<>();

        for (EccentricityRecord record : inspectionRecords) {

            if (record.getInspectionId() == null) {
                return false;
            }

            if (!expectedInspectionId.equals(
                    record.getInspectionId())) {
                return false;
            }

            String position = record.getPosition();

            if (position == null) {
                return false;
            }

            position = position.trim().toUpperCase(Locale.ROOT);

            // Support both REAR and BACK for historical records.
            if ("REAR".equals(position)) {
                position = "BACK";
            }

            if (!expectedPositions.contains(position)) {
                return false;
            }

            if (!actualPositions.add(position)) {
                return false;
            }

            if (record.getReferenceWeight() == null
                    || record.getObservedWeight() == null) {
                return false;
            }
        }

        return actualPositions.size() == 5;
    }

    // =========================================================
    // HELPER 4: ROUND VALUES
    // =========================================================

    /*
     * Round measurement values to four decimal places.
     */

    private double round(double value) {

        return Math.round(
                value * 10000.0
        ) / 10000.0;
    }

    // =========================================================
    // HELPER 5: HOLD A TREND ENTRY WITH ITS TIMESTAMP
    // =========================================================

    /*
     * The timestamp is retained internally so all test types
     * can be sorted chronologically before returning the response.
     */

    private record TrendEntry(
            LocalDateTime createdAt,
            AdvancedAnalyticsResponse.TrendPoint point) {
    }
}
