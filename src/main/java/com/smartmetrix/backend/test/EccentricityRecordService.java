package com.smartmetrix.backend.test;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EccentricityRecordService {

    private final EccentricityRecordRepository eccentricityRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;

    public EccentricityRecordService(
            EccentricityRecordRepository eccentricityRecordRepository,
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository) {

        this.eccentricityRecordRepository =
                eccentricityRecordRepository;

        this.inspectionRepository =
                inspectionRepository;

        this.instrumentRepository =
                instrumentRepository;
    }

    // -----------------------------------------
    // CREATE RECORD
    // -----------------------------------------

    public EccentricityRecord createRecord(
            EccentricityRecord record) {

        inspectionRepository.findById(record.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        if (record.getPosition() == null
                || record.getPosition().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Position is required");
        }

        String position =
                record.getPosition()
                        .trim()
                        .toUpperCase();

        if (!isValidPosition(position)) {

            throw new IllegalArgumentException(
                    "Position must be LEFT, RIGHT, FRONT, BACK or CENTER");
        }

        record.setPosition(position);

        List<EccentricityRecord> existingRecords =
                eccentricityRecordRepository
                        .findByInspectionId(
                                record.getInspectionId());

        for (EccentricityRecord existing :
                existingRecords) {

            if (existing.getPosition()
                    .equalsIgnoreCase(position)) {

                throw new IllegalArgumentException(
                        "Eccentricity position already exists");
            }
        }

        return eccentricityRecordRepository.save(record);
    }

    // -----------------------------------------
    // GET ALL
    // -----------------------------------------

    public List<EccentricityRecord> getAllRecords() {

        return eccentricityRecordRepository.findAll();
    }

    // -----------------------------------------
    // GET BY INSPECTION
    // -----------------------------------------

    public List<EccentricityRecord> getRecordsByInspectionId(
            Long inspectionId) {

        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        return eccentricityRecordRepository
                .findByInspectionId(inspectionId);
    }

    // -----------------------------------------
    // MAX DIFFERENCE
    // -----------------------------------------

    public double calculateMaximumDifference(
            Long inspectionId) {

        List<EccentricityRecord> records =
                getRecordsByInspectionId(inspectionId);

        if (records.isEmpty()) {
            return 0.0;
        }

        double min =
                records.get(0).getObservedWeight();

        double max =
                records.get(0).getObservedWeight();

        for (EccentricityRecord record : records) {

            double observed =
                    record.getObservedWeight();

            if (observed < min) {
                min = observed;
            }

            if (observed > max) {
                max = observed;
            }
        }

        double difference =
                max - min;

        return round(difference);
    }

    // -----------------------------------------
    // COMPLETE SUMMARY
    // -----------------------------------------

    public EccentricitySummaryResponse getSummary(
            Long inspectionId) {

        List<EccentricityRecord> records =
                getRecordsByInspectionId(inspectionId);

        if (records.isEmpty()) {

            throw new IllegalArgumentException(
                    "No eccentricity readings found");
        }

        if (records.size() < 5) {

            throw new IllegalArgumentException(
                    "Eccentricity test requires 5 positions");
        }

        records.sort(
                Comparator.comparing(
                        EccentricityRecord::getPosition));

        EccentricityRecord firstRecord =
                records.get(0);

        Double referenceWeight =
                firstRecord.getReferenceWeight();

        // -----------------------------------------
        // GET INSPECTION
        // -----------------------------------------

        Inspection inspection =
                inspectionRepository.findById(inspectionId)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        // -----------------------------------------
        // GET INSTRUMENT
        // -----------------------------------------

        Instrument instrument =
                instrumentRepository.findById(
                                inspection.getInstrumentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Instrument not found"));

        double scaleInterval =
                instrument.getScaleInterval();

        String instrumentClass =
                instrument.getInstrumentClass();

        // -----------------------------------------
        // CALCULATE MPE
        // -----------------------------------------

        double mpe =
                calculateMpe(
                        referenceWeight,
                        scaleInterval,
                        instrumentClass);

        // -----------------------------------------
        // POSITION RESULTS
        // -----------------------------------------

        List<EccentricityPositionResult> positions =
                new ArrayList<>();

        boolean allPassed = true;

        double minObserved =
                Double.MAX_VALUE;

        double maxObserved =
                -Double.MAX_VALUE;

        for (EccentricityRecord record :
                records) {

            double observed =
                    record.getObservedWeight();

            double error =
                    observed - referenceWeight;

            String positionResult;

            if (Math.abs(error) <= mpe) {

                positionResult = "PASS";

            } else {

                positionResult = "FAIL";

                allPassed = false;
            }

            if (observed < minObserved) {
                minObserved = observed;
            }

            if (observed > maxObserved) {
                maxObserved = observed;
            }

            positions.add(
                    new EccentricityPositionResult(
                            record.getPosition(),
                            referenceWeight,
                            observed,
                            round(error),
                            positionResult
                    )
            );
        }

        // -----------------------------------------
        // MAXIMUM DIFFERENCE
        // -----------------------------------------

        double maximumDifference =
                maxObserved - minObserved;

        maximumDifference =
                round(maximumDifference);

        // -----------------------------------------
        // OVERALL RESULT
        // -----------------------------------------

        String result;

        if (allPassed) {
            result = "PASS";
        } else {
            result = "FAIL";
        }

        return new EccentricitySummaryResponse(
                inspectionId,
                referenceWeight,
                positions,
                maximumDifference,
                round(mpe),
                result
        );
    }

    // -----------------------------------------
    // VALID POSITION
    // -----------------------------------------

    private boolean isValidPosition(
            String position) {

        return position.equals("LEFT")
                || position.equals("RIGHT")
                || position.equals("FRONT")
                || position.equals("BACK")
                || position.equals("CENTER");
    }

    // -----------------------------------------
    // MPE CALCULATION
    // -----------------------------------------

    private double calculateMpe(
            double load,
            double e,
            String instrumentClass) {

        if (e <= 0) {

            throw new IllegalArgumentException(
                    "Scale interval must be greater than zero");
        }

        if (instrumentClass == null) {

            throw new IllegalArgumentException(
                    "Instrument class is required");
        }

        double multiplier;

        String clazz =
                instrumentClass
                        .trim()
                        .toUpperCase();

        switch (clazz) {

            case "I":

                if (load <= 50000 * e) {

                    multiplier = 0.5;

                } else if (load <= 200000 * e) {

                    multiplier = 1.0;

                } else {

                    multiplier = 1.5;
                }

                break;

            case "II":

                if (load <= 5000 * e) {

                    multiplier = 0.5;

                } else if (load <= 20000 * e) {

                    multiplier = 1.0;

                } else {

                    multiplier = 1.5;
                }

                break;

            case "III":

                if (load <= 500 * e) {

                    multiplier = 0.5;

                } else if (load <= 2000 * e) {

                    multiplier = 1.0;

                } else {

                    multiplier = 1.5;
                }

                break;

            case "IIII":

                if (load <= 50 * e) {

                    multiplier = 0.5;

                } else if (load <= 200 * e) {

                    multiplier = 1.0;

                } else {

                    multiplier = 1.5;
                }

                break;

            default:

                throw new IllegalArgumentException(
                        "Invalid instrument class: "
                                + instrumentClass);
        }

        return multiplier * e;
    }

    // -----------------------------------------
    // ROUND
    // -----------------------------------------

    private double round(double value) {

        return Math.round(value * 10000.0) / 10000.0;
    }
}