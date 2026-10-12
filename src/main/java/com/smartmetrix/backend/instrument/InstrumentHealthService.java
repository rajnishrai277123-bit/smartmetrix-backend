
package com.smartmetrix.backend.instrument;

import com.smartmetrix.backend.instrument.dto.InstrumentHealthResponse;
import com.smartmetrix.backend.instrument.exception.InstrumentNotFoundException;
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

    public List<InstrumentHealthResponse> getAllInstrumentHealth() {
        return instrumentRepository.findAll()
                .stream()
                .map(instrument -> getInstrumentHealth(instrument.getId()))
                .toList();
    }

    public InstrumentHealthResponse getInstrumentHealth(Long instrumentId) {

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() ->
                        new InstrumentNotFoundException("Instrument not found"));

        List<Inspection> inspections =
                inspectionRepository.findByInstrumentId(instrumentId);

        List<Inspection> completedInspections = inspections.stream()
                .filter(i ->
                        Inspection.RESULT_PASS.equalsIgnoreCase(i.getOverallResult())
                                || Inspection.RESULT_FAIL.equalsIgnoreCase(i.getOverallResult()))
                .toList();

        int passedInspections = (int) completedInspections.stream()
                .filter(i -> Inspection.RESULT_PASS.equalsIgnoreCase(i.getOverallResult()))
                .count();

        int failedInspections = (int) completedInspections.stream()
                .filter(i -> Inspection.RESULT_FAIL.equalsIgnoreCase(i.getOverallResult()))
                .count();

        int totalInspections = completedInspections.size();

        double passRate = totalInspections == 0
                ? 0.0
                : (passedInspections * 100.0) / totalInspections;

        List<Double> wpScores = new ArrayList<>();
        List<Double> repeatabilityScores = new ArrayList<>();
        List<Double> eccentricityScores = new ArrayList<>();

        for (Inspection inspection : completedInspections) {

            // Weighing performance quality
            List<TestRecord> testRecords =
                    testRecordRepository.findByInspectionId(inspection.getId());

            for (TestRecord record : testRecords) {

                if (!"WEIGHING_PERFORMANCE".equalsIgnoreCase(
                        String.valueOf(record.getTestType()))) {
                    continue;
                }

                Double error = record.getError();
                Double mpe = record.getMpe();

                if (error == null || mpe == null || mpe <= 0) {
                    continue;
                }

                double quality = calculateQuality(Math.abs(error), mpe);
                wpScores.add(quality);
            }

            // Repeatability quality
            List<RepeatabilityRecord> repeatabilityRecords =
                    repeatabilityRecordRepository.findByInspectionId(
                            inspection.getId());

            List<Double> repeatabilityWeights = new ArrayList<>();

            for (RepeatabilityRecord record : repeatabilityRecords) {
                if (record.getObservedWeight() != null) {
                    repeatabilityWeights.add(record.getObservedWeight());
                }
            }

            Double scaleInterval = instrument.getScaleInterval();

            if (repeatabilityWeights.size() >= 2
                    && scaleInterval != null
                    && scaleInterval > 0) {

                double min = repeatabilityWeights.stream()
                        .min(Double::compareTo)
                        .orElse(0.0);

                double max = repeatabilityWeights.stream()
                        .max(Double::compareTo)
                        .orElse(0.0);

                double range = max - min;

                double quality = calculateQuality(range, scaleInterval);
                repeatabilityScores.add(quality);
            }

            // Eccentricity quality
            List<EccentricityRecord> eccentricityRecords =
                    eccentricityRecordRepository.findByInspectionId(
                            inspection.getId());

            List<Double> eccentricityWeights = new ArrayList<>();

            for (EccentricityRecord record : eccentricityRecords) {
                if (record.getObservedWeight() != null) {
                    eccentricityWeights.add(record.getObservedWeight());
                }
            }

            if (eccentricityWeights.size() >= 2
                    && scaleInterval != null
                    && scaleInterval > 0) {

                double min = eccentricityWeights.stream()
                        .min(Double::compareTo)
                        .orElse(0.0);

                double max = eccentricityWeights.stream()
                        .max(Double::compareTo)
                        .orElse(0.0);

                double range = max - min;

                double quality = calculateQuality(range, scaleInterval);
                eccentricityScores.add(quality);
            }
        }

        double wpQuality = average(wpScores);
        double repeatabilityQuality = average(repeatabilityScores);
        double eccentricityQuality = average(eccentricityScores);

        boolean hasWP = !wpScores.isEmpty();
        boolean hasRepeatability = !repeatabilityScores.isEmpty();
        boolean hasEccentricity = !eccentricityScores.isEmpty();

        double healthScore = 0.0;
        String healthStatus;

        if (totalInspections == 0) {

            healthStatus = "NO_DATA";

        } else {

            /*
             * Base weights:
             * Pass rate      = 40%
             * Weighing test  = 30%
             * Repeatability  = 15%
             * Eccentricity   = 15%
             *
             * Missing test types are excluded.
             * Remaining weights are normalized automatically.
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

            healthScore = weightedScore / totalWeight;

            if (healthScore >= 80.0) {
                healthStatus = "HEALTHY";
            } else if (healthScore >= 50.0) {
                healthStatus = "WARNING";
            } else {
                healthStatus = "CRITICAL";
            }
        }

        return new InstrumentHealthResponse(
                instrument.getId(),
                instrument.getSerialNumber(),
                instrument.getModel(),
                totalInspections,
                passedInspections,
                failedInspections,
                round(passRate),
                round(wpQuality),
                round(repeatabilityQuality),
                round(eccentricityQuality),
                round(healthScore),
                healthStatus
        );
    }

    /*
     * Prototype scoring policy:
     * At 0% of limit: quality = 100
     * At 100% of limit: quality = 50
     * At 200% or more: quality = 0
     *
     * This is a project-defined score, not an official OIML formula.
     */
    private double calculateQuality(
            double measuredValue,
            double allowedLimit) {

        if (allowedLimit <= 0) {
            return 0.0;
        }

        double ratio = Math.abs(measuredValue) / allowedLimit;
        double quality = 100.0 - (ratio * 50.0);

        return Math.max(0.0, Math.min(100.0, quality));
    }

    private double average(List<Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (Double score : scores) {
            total += score;
        }

        return total / scores.size();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
