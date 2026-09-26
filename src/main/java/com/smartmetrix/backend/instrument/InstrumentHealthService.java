package com.smartmetrix.backend.instrument;

import com.smartmetrix.backend.instrument.dto.InstrumentHealthResponse;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.EccentricityRecord;
import com.smartmetrix.backend.test.EccentricityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecord;
import com.smartmetrix.backend.test.RepeatabilityRecordRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstrumentHealthService {

    private final InstrumentRepository instrumentRepository;
    private final InspectionRepository inspectionRepository;
    private final TestRecordRepository testRecordRepository;
    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final EccentricityRecordRepository eccentricityRecordRepository;

    public InstrumentHealthService(
            InstrumentRepository instrumentRepository,
            InspectionRepository inspectionRepository,
            TestRecordRepository testRecordRepository,
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            EccentricityRecordRepository eccentricityRecordRepository) {

        this.instrumentRepository = instrumentRepository;
        this.inspectionRepository = inspectionRepository;
        this.testRecordRepository = testRecordRepository;
        this.repeatabilityRecordRepository = repeatabilityRecordRepository;
        this.eccentricityRecordRepository = eccentricityRecordRepository;
    }

    public InstrumentHealthResponse getInstrumentHealth(Long instrumentId) {

        // ==========================================
        // 1. CHECK INSTRUMENT
        // ==========================================

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() ->
                        new RuntimeException("Instrument not found"));


        // ==========================================
        // 2. GET ALL INSPECTIONS
        // ==========================================

        List<Inspection> inspections =
                inspectionRepository.findByInstrumentId(instrumentId);

        int totalInspections = inspections.size();


        // ==========================================
        // 3. COUNT PASS AND FAIL
        // ==========================================

        int passedInspections = (int) inspections.stream()
                .filter(i -> Inspection.RESULT_PASS.equalsIgnoreCase(
                        i.getOverallResult()))
                .count();

        int failedInspections = (int) inspections.stream()
                .filter(i -> Inspection.RESULT_FAIL.equalsIgnoreCase(
                        i.getOverallResult()))
                .count();


        // ==========================================
        // 4. PASS RATE
        // ==========================================

        double passRate = 0.0;

        if (totalInspections > 0) {
            passRate =
                    ((double) passedInspections / totalInspections) * 100;
        }


        // ==========================================
        // 5. WEIGHING PERFORMANCE QUALITY
        // ==========================================

        List<Double> wpQualityScores = new ArrayList<>();

        for (Inspection inspection : inspections) {

            List<TestRecord> testRecords =
                    testRecordRepository.findByInspectionId(
                            inspection.getId());

            for (TestRecord record : testRecords) {

                if (!"WEIGHING_PERFORMANCE".equalsIgnoreCase(
                        record.getTestType())) {
                    continue;
                }

                Double error = record.getError();
                Double mpe = record.getMpe();

                if (error == null || mpe == null || mpe <= 0) {
                    continue;
                }

                double absoluteError = Math.abs(error);

                double quality;

                if (absoluteError >= mpe) {
                    quality = 0.0;
                } else {
                    quality =
                            (1.0 - (absoluteError / mpe)) * 100.0;
                }

                wpQualityScores.add(quality);
            }
        }


        // ==========================================
        // 6. AVERAGE WP QUALITY
        // ==========================================

        double wpQuality = 0.0;

        if (!wpQualityScores.isEmpty()) {

            double totalQuality = 0.0;

            for (Double score : wpQualityScores) {
                totalQuality += score;
            }

            wpQuality =
                    totalQuality / wpQualityScores.size();
        }


        // ==========================================
        // 7. REPEATABILITY QUALITY
        // ==========================================

        List<Double> repeatabilityQualityScores =
                new ArrayList<>();

        for (Inspection inspection : inspections) {

            List<RepeatabilityRecord> records =
                    repeatabilityRecordRepository
                            .findByInspectionId(inspection.getId());

            if (records.isEmpty()) {
                continue;
            }

            List<Double> observedWeights =
                    new ArrayList<>();

            for (RepeatabilityRecord record : records) {

                if (record.getObservedWeight() != null) {
                    observedWeights.add(
                            record.getObservedWeight());
                }
            }

            if (observedWeights.size() < 2) {
                continue;
            }

            double minimum =
                    observedWeights.stream()
                            .min(Double::compareTo)
                            .orElse(0.0);

            double maximum =
                    observedWeights.stream()
                            .max(Double::compareTo)
                            .orElse(0.0);

            double range = maximum - minimum;

            Double scaleInterval =
                    instrument.getScaleInterval();

            if (scaleInterval == null || scaleInterval <= 0) {
                continue;
            }

            double quality;

            if (range >= scaleInterval) {
                quality = 0.0;
            } else {
                quality =
                        (1.0 - (range / scaleInterval)) * 100.0;
            }

            repeatabilityQualityScores.add(quality);
        }


        // ==========================================
        // 8. AVERAGE REPEATABILITY QUALITY
        // ==========================================

        double repeatabilityQuality = 0.0;

        if (!repeatabilityQualityScores.isEmpty()) {

            double totalQuality = 0.0;

            for (Double score : repeatabilityQualityScores) {
                totalQuality += score;
            }

            repeatabilityQuality =
                    totalQuality /
                            repeatabilityQualityScores.size();
        }


        // ==========================================
        // 9. ECCENTRICITY QUALITY
        // ==========================================

        List<Double> eccentricityQualityScores =
                new ArrayList<>();

        for (Inspection inspection : inspections) {

            List<EccentricityRecord> records =
                    eccentricityRecordRepository
                            .findByInspectionId(
                                    inspection.getId());

            if (records.isEmpty()) {
                continue;
            }

            List<Double> observedWeights =
                    new ArrayList<>();

            for (EccentricityRecord record : records) {

                if (record.getObservedWeight() != null) {
                    observedWeights.add(
                            record.getObservedWeight());
                }
            }

            if (observedWeights.size() < 2) {
                continue;
            }

            double minimum =
                    observedWeights.stream()
                            .min(Double::compareTo)
                            .orElse(0.0);

            double maximum =
                    observedWeights.stream()
                            .max(Double::compareTo)
                            .orElse(0.0);

            double range = maximum - minimum;

            Double scaleInterval =
                    instrument.getScaleInterval();

            if (scaleInterval == null || scaleInterval <= 0) {
                continue;
            }

            double quality;

            if (range >= scaleInterval) {
                quality = 0.0;
            } else {
                quality =
                        (1.0 - (range / scaleInterval)) * 100.0;
            }

            eccentricityQualityScores.add(quality);
        }


        // ==========================================
        // 10. AVERAGE ECCENTRICITY QUALITY
        // ==========================================

        double eccentricityQuality = 0.0;

        if (!eccentricityQualityScores.isEmpty()) {

            double totalQuality = 0.0;

            for (Double score : eccentricityQualityScores) {
                totalQuality += score;
            }

            eccentricityQuality =
                    totalQuality /
                            eccentricityQualityScores.size();
        }


        // ==========================================
        // 11. CHECK AVAILABLE QUALITY DATA
        // ==========================================

        boolean hasWP = !wpQualityScores.isEmpty();

        boolean hasRepeatability =
                !repeatabilityQualityScores.isEmpty();

        boolean hasEccentricity =
                !eccentricityQualityScores.isEmpty();


        // ==========================================
        // 12. FINAL HEALTH SCORE
        // ==========================================

        double healthScore;

        if (totalInspections == 0) {

            healthScore = 0.0;

        } else if (!hasWP
                && !hasRepeatability
                && !hasEccentricity) {

            healthScore = passRate;

        } else {

            /*
             * Final Health Score:
             *
             * Pass Rate       = 40%
             * WP Quality      = 30%
             * Repeatability   = 15%
             * Eccentricity    = 15%
             */

            double weightedScore = passRate * 0.40;
            double totalWeight = 0.40;

            if (hasWP) {
                weightedScore += wpQuality * 0.30;
                totalWeight += 0.30;
            }

            if (hasRepeatability) {
                weightedScore += repeatabilityQuality * 0.15;
                totalWeight += 0.15;
            }

            if (hasEccentricity) {
                weightedScore += eccentricityQuality * 0.15;
                totalWeight += 0.15;
            }

            healthScore =
                    weightedScore / totalWeight;
        }


        // ==========================================
        // 13. HEALTH STATUS
        // ==========================================

        String healthStatus;

        if (totalInspections == 0) {

            healthStatus = "NO_DATA";

        } else if (healthScore >= 80) {

            healthStatus = "HEALTHY";

        } else if (healthScore >= 50) {

            healthStatus = "WARNING";

        } else {

            healthStatus = "CRITICAL";
        }


        // ==========================================
        // 14. ROUND VALUES
        // ==========================================

        double roundedPassRate =
                Math.round(passRate * 100.0) / 100.0;

        double roundedWpQuality =
                Math.round(wpQuality * 100.0) / 100.0;

        double roundedRepeatabilityQuality =
                Math.round(repeatabilityQuality * 100.0) / 100.0;

        double roundedEccentricityQuality =
                Math.round(eccentricityQuality * 100.0) / 100.0;

        double roundedHealthScore =
                Math.round(healthScore * 100.0) / 100.0;


        // ==========================================
        // 15. RETURN RESPONSE
        // ==========================================

        return new InstrumentHealthResponse(
                instrument.getId(),
                instrument.getSerialNumber(),
                instrument.getModel(),
                totalInspections,
                passedInspections,
                failedInspections,
                roundedPassRate,
                roundedWpQuality,
                roundedRepeatabilityQuality,
                roundedEccentricityQuality,
                roundedHealthScore,
                healthStatus
        );
    }
}
