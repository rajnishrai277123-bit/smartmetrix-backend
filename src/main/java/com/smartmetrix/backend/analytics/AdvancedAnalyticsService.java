package com.smartmetrix.backend.analytics;

import com.smartmetrix.backend.environment.EnvironmentRecord;
import com.smartmetrix.backend.environment.EnvironmentRecordRepository;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
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
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdvancedAnalyticsService {

    private final TestRecordRepository testRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final EnvironmentRecordRepository environmentRecordRepository;
    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final EccentricityRecordRepository eccentricityRecordRepository;

    private final RepeatabilityRecordService repeatabilityRecordService;
    private final EccentricityRecordService eccentricityRecordService;

    public AdvancedAnalyticsService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository,
            EnvironmentRecordRepository environmentRecordRepository,
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            EccentricityRecordRepository eccentricityRecordRepository,
            RepeatabilityRecordService repeatabilityRecordService,
            EccentricityRecordService eccentricityRecordService) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.environmentRecordRepository = environmentRecordRepository;
        this.repeatabilityRecordRepository = repeatabilityRecordRepository;
        this.eccentricityRecordRepository = eccentricityRecordRepository;

        this.repeatabilityRecordService =
                repeatabilityRecordService;

        this.eccentricityRecordService =
                eccentricityRecordService;
    }

    public AdvancedAnalyticsResponse analyzeInstrument(
            Long instrumentId) {

        if (instrumentId == null) {
            throw new IllegalArgumentException(
                    "Instrument ID is required");
        }

        // =====================================================
        // 1. WEIGHING PERFORMANCE RECORDS
        // =====================================================

        List<TestRecord> allRecords =
                testRecordRepository.findAll();

        List<TestRecord> records =
                new ArrayList<>();

        /*
         * Only WEIGHING_PERFORMANCE records are used
         * for measurement-error statistics and trend.
         *
         * Repeatability and Eccentricity remain
         * separate test types.
         */

        for (TestRecord record : allRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            if (!"WEIGHING_PERFORMANCE".equals(
                    record.getTestType())) {
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

                records.add(record);
            }
        }

        // =====================================================
        // 2. REPEATABILITY TESTS
        // =====================================================

        List<RepeatabilityRecord>
                allRepeatabilityRecords =
                repeatabilityRecordRepository.findAll();

        /*
         * One testRunId = ONE repeatability test.
         *
         * 5 readings inside one testRunId
         * are counted as one test.
         */

        Set<Long> repeatabilityTestRunIds =
                new HashSet<>();

        for (RepeatabilityRecord record :
                allRepeatabilityRecords) {

            if (record.getInspectionId() == null) {
                continue;
            }

            if (record.getTestRunId() == null) {
                continue;
            }

            Inspection inspection =
                    inspectionRepository
                            .findById(record.getInspectionId())
                            .orElse(null);

            if (inspection == null) {
                continue;
            }

            if (!instrumentId.equals(
                    inspection.getInstrumentId())) {
                continue;
            }

            List<RepeatabilityRecord> runRecords =
                    repeatabilityRecordRepository
                            .findByTestRunId(
                                    record.getTestRunId());

            /*
             * A completed repeatability test
             * requires exactly 5 readings.
             */
            if (runRecords.size() == 5) {

                repeatabilityTestRunIds.add(
                        record.getTestRunId());
            }
        }

        // =====================================================
        // 3. ECCENTRICITY TESTS
        // =====================================================

        List<EccentricityRecord>
                allEccentricityRecords =
                eccentricityRecordRepository.findAll();

        /*
         * One inspection = ONE eccentricity test.
         *
         * The 5 positions are:
         * LEFT
         * RIGHT
         * FRONT
         * BACK
         * CENTER
         */

        Set<Long> eccentricityInspectionIds =
                new HashSet<>();

        for (EccentricityRecord record :
                allEccentricityRecords) {

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

            if (!instrumentId.equals(
                    inspection.getInstrumentId())) {
                continue;
            }

            List<EccentricityRecord> inspectionRecords =
                    eccentricityRecordRepository
                            .findByInspectionId(
                                    record.getInspectionId());

            /*
             * A completed eccentricity test
             * requires exactly 5 positions.
             */
            if (inspectionRecords.size() == 5) {

                eccentricityInspectionIds.add(
                        record.getInspectionId());
            }
        }

        // =====================================================
        // 4. CHECK WHETHER ANY ANALYTICS DATA EXISTS
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
                    "Perform more weighing, repeatability or eccentricity tests before prediction.",
                    new ArrayList<>()
            );
        }

        // =====================================================
        // 5. TOTAL TEST COUNT
        // =====================================================

        long weighingPerformanceTests =
                records.size();

        long repeatabilityTests =
                repeatabilityTestRunIds.size();

        long eccentricityTests =
                eccentricityInspectionIds.size();

        /*
         * Actual test executions:
         *
         * WP              = 1 TestRecord = 1 test
         * Repeatability   = 1 testRunId = 1 test
         * Eccentricity    = 1 inspection = 1 test
         */

        long totalTests =
                weighingPerformanceTests
                        + repeatabilityTests
                        + eccentricityTests;
        // =====================================================
        // 6. FAILED TESTS
        // =====================================================

        /*
         * Start with failed Weighing Performance tests.
         */

        long failedTests =
                records.stream()
                        .filter(r ->
                                "FAIL".equals(r.getResult()))
                        .count();

        // -----------------------------------------------------
        // REPEATABILITY FAILED RUNS
        // -----------------------------------------------------

        for (Long testRunId :
                repeatabilityTestRunIds) {

            try {

                RepeatabilitySummaryResponse summary =
                        repeatabilityRecordService
                                .getSummary(testRunId);

                if ("FAIL".equals(
                        summary.getResult())) {

                    failedTests++;
                }

            } catch (Exception ignored) {

                /*
                 * Ignore invalid/incomplete historical
                 * repeatability runs.
                 */
            }
        }

        // -----------------------------------------------------
        // ECCENTRICITY FAILED TESTS
        // -----------------------------------------------------

        for (Long inspectionId :
                eccentricityInspectionIds) {

            try {

                EccentricitySummaryResponse summary =
                        eccentricityRecordService
                                .getSummary(inspectionId);

                if ("FAIL".equals(
                        summary.getResult())) {

                    failedTests++;
                }

            } catch (Exception ignored) {

                /*
                 * Ignore invalid/incomplete historical
                 * eccentricity tests.
                 */
            }
        }

        // =====================================================
        // 7. WEIGHING PERFORMANCE ERROR STATISTICS
        // =====================================================

        /*
         * Error statistics remain WP-only.
         *
         * We do NOT mix:
         *
         * WP error
         * Repeatability range
         * Eccentricity maximum difference
         *
         * into one error value.
         */

        double averageError =
                records.stream()
                        .mapToDouble(r ->
                                Math.abs(
                                        r.getError() == null
                                                ? 0
                                                : r.getError()))
                        .average()
                        .orElse(0);

        double maximumAbsoluteError =
                records.stream()
                        .mapToDouble(r ->
                                Math.abs(
                                        r.getError() == null
                                                ? 0
                                                : r.getError()))
                        .max()
                        .orElse(0);

        // =====================================================
        // 8. INSTRUMENT HEALTH SCORE
        // =====================================================

        double healthScore = 100;

        /*
         * Every failed test execution gives
         * a failure penalty.
         */

        healthScore -=
                failedTests * 10;

        /*
         * WP average error penalty.
         */

        healthScore -=
                Math.min(
                        30,
                        averageError * 100
                );

        /*
         * WP maximum error penalty.
         */

        healthScore -=
                Math.min(
                        20,
                        maximumAbsoluteError * 100
                );

        healthScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                healthScore
                        )
                );

        healthScore =
                Math.round(
                        healthScore * 100.0
                ) / 100.0;

        String healthStatus;

        if (totalTests < 3) {

            healthStatus =
                    "INSUFFICIENT_DATA";

        } else if (healthScore >= 80) {

            healthStatus =
                    "HEALTHY";

        } else if (healthScore >= 60) {

            healthStatus =
                    "WARNING";

        } else {

            healthStatus =
                    "CRITICAL";
        }

        // =====================================================
        // 9. ENVIRONMENT IMPACT
        // =====================================================

        /*
         * Environment data belongs to an inspection.
         *
         * We collect unique inspection IDs associated with:
         *
         * - Weighing Performance
         * - Repeatability
         * - Eccentricity
         */

        Set<Long> analyticsInspectionIds =
                new HashSet<>();

        // -----------------------------------------------------
        // WP inspections
        // -----------------------------------------------------

        for (TestRecord record : records) {

            if (record.getInspectionId() != null) {

                analyticsInspectionIds.add(
                        record.getInspectionId());
            }
        }

        // -----------------------------------------------------
        // Repeatability inspections
        // -----------------------------------------------------

        for (RepeatabilityRecord record :
                allRepeatabilityRecords) {

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

                analyticsInspectionIds.add(
                        record.getInspectionId());
            }
        }

        // -----------------------------------------------------
        // Eccentricity inspections
        // -----------------------------------------------------

        for (EccentricityRecord record :
                allEccentricityRecords) {

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

                analyticsInspectionIds.add(
                        record.getInspectionId());
            }
        }

        double environmentImpactScore = 0;

        int environmentSamples = 0;

        for (Long inspectionId :
                analyticsInspectionIds) {

            List<EnvironmentRecord>
                    environmentRecords =
                    environmentRecordRepository
                            .findByInspectionId(
                                    inspectionId);

            for (EnvironmentRecord environmentRecord :
                    environmentRecords) {

                String status =
                        environmentRecord.getStatus();

                if ("OUT_OF_RANGE".equals(status)) {

                    environmentImpactScore += 20;

                } else if ("WARNING".equals(status)) {

                    environmentImpactScore += 10;

                } else if ("NORMAL".equals(status)) {

                    environmentImpactScore += 0;
                }

                environmentSamples++;
            }
        }

        if (environmentSamples > 0) {

            environmentImpactScore =
                    environmentImpactScore
                            / environmentSamples;
        }

        environmentImpactScore =
                Math.min(
                        100,
                        environmentImpactScore
                );

        environmentImpactScore =
                Math.round(
                        environmentImpactScore * 100.0
                ) / 100.0;

        String environmentImpactLevel;

        if (environmentSamples == 0) {

            environmentImpactLevel =
                    "INSUFFICIENT_DATA";

        } else if (environmentImpactScore < 20) {

            environmentImpactLevel =
                    "LOW";

        } else if (environmentImpactScore < 50) {

            environmentImpactLevel =
                    "MEDIUM";

        } else {

            environmentImpactLevel =
                    "HIGH";
        }

        // =====================================================
        // 10. RECALIBRATION PREDICTION
        // =====================================================

        String recalibrationPrediction;
        String recalibrationRecommendation;

        if (totalTests < 3) {

            recalibrationPrediction =
                    "INSUFFICIENT_DATA";

            recalibrationRecommendation =
                    "Collect at least 3 completed historical tests.";

        } else if (healthScore < 60 ||
                failedTests >= 3) {

            recalibrationPrediction =
                    "HIGH_RISK";

            recalibrationRecommendation =
                    "Schedule recalibration or detailed inspection soon.";

        } else if (healthScore < 80 ||
                averageError > 0.02) {

            recalibrationPrediction =
                    "MONITOR";

            recalibrationRecommendation =
                    "Increase monitoring and consider recalibration.";

        } else {

            recalibrationPrediction =
                    "LOW_RISK";

            recalibrationRecommendation =
                    "Continue routine inspection schedule.";
        }

        // =====================================================
        // 11. DATE-WISE WEIGHING PERFORMANCE TREND
        // =====================================================

        /*
         * Trend remains WP-only.
         *
         * Repeatability and Eccentricity are not converted
         * into WP error values.
         */

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd MMM yyyy"
                );

        List<AdvancedAnalyticsResponse.TrendPoint>
                trend =
                records.stream()
                        .filter(r ->
                                r.getCreatedAt() != null)
                        .sorted(
                                Comparator.comparing(
                                        TestRecord::getCreatedAt
                                )
                        )
                        .map(r ->
                                new AdvancedAnalyticsResponse.TrendPoint(
                                        r.getCreatedAt()
                                                .format(formatter),

                                        Math.abs(
                                                r.getError() == null
                                                        ? 0
                                                        : r.getError()
                                        ),

                                        r.getResult()
                                )
                        )
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
    // ROUND
    // =========================================================

    private double round(double value) {

        return Math.round(
                value * 10000.0
        ) / 10000.0;
    }
}