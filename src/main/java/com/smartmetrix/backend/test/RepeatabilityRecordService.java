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
public class RepeatabilityRecordService {

    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;

    public RepeatabilityRecordService(
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository) {

        this.repeatabilityRecordRepository = repeatabilityRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.instrumentRepository = instrumentRepository;
    }

    // -----------------------------------------
    // CREATE RECORD
    // -----------------------------------------

    public RepeatabilityRecord createRecord(
            RepeatabilityRecord record) {

        inspectionRepository.findById(record.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        if (record.getTestRunId() == null) {
            throw new IllegalArgumentException(
                    "testRunId is required");
        }

        if (record.getReadingNumber() == null) {
            throw new IllegalArgumentException(
                    "readingNumber is required");
        }

        if (record.getReadingNumber() < 1
                || record.getReadingNumber() > 5) {

            throw new IllegalArgumentException(
                    "Reading number must be between 1 and 5");
        }

        List<RepeatabilityRecord> existingRecords =
                repeatabilityRecordRepository
                        .findByTestRunId(record.getTestRunId());

        for (RepeatabilityRecord existing : existingRecords) {

            if (existing.getReadingNumber()
                    .equals(record.getReadingNumber())) {

                throw new IllegalArgumentException(
                        "Reading number already exists in this test run");
            }
        }

        if (existingRecords.size() >= 5) {

            throw new IllegalArgumentException(
                    "A repeatability test run can have maximum 5 readings");
        }

        return repeatabilityRecordRepository.save(record);
    }

    // -----------------------------------------
    // GET ALL RECORDS
    // -----------------------------------------

    public List<RepeatabilityRecord> getAllRecords() {

        return repeatabilityRecordRepository.findAll();
    }

    // -----------------------------------------
    // GET BY INSPECTION
    // -----------------------------------------

    public List<RepeatabilityRecord> getRecordsByInspectionId(
            Long inspectionId) {

        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        return repeatabilityRecordRepository
                .findByInspectionId(inspectionId);
    }

    // -----------------------------------------
    // GET BY TEST RUN
    // -----------------------------------------

    public List<RepeatabilityRecord> getRecordsByTestRunId(
            Long testRunId) {

        return repeatabilityRecordRepository
                .findByTestRunId(testRunId);
    }

    // -----------------------------------------
    // CALCULATE AVERAGE
    // -----------------------------------------

    public double calculateAverage(Long testRunId) {

        List<RepeatabilityRecord> records =
                getRecordsByTestRunId(testRunId);

        if (records.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;

        for (RepeatabilityRecord record : records) {

            sum += record.getObservedWeight();
        }

        double average =
                sum / records.size();

        return Math.round(average * 10000.0) / 10000.0;
    }

    // -----------------------------------------
    // CALCULATE RANGE
    // -----------------------------------------

    public double calculateRange(Long testRunId) {

        List<RepeatabilityRecord> records =
                getRecordsByTestRunId(testRunId);

        if (records.isEmpty()) {
            return 0.0;
        }

        double min =
                records.get(0).getObservedWeight();

        double max =
                records.get(0).getObservedWeight();

        for (RepeatabilityRecord record : records) {

            double reading =
                    record.getObservedWeight();

            if (reading < min) {
                min = reading;
            }

            if (reading > max) {
                max = reading;
            }
        }

        double range =
                max - min;

        return Math.round(range * 10000.0) / 10000.0;
    }

    // -----------------------------------------
    // REPEATABILITY SUMMARY
    // -----------------------------------------

    public RepeatabilitySummaryResponse getSummary(
            Long testRunId) {

        List<RepeatabilityRecord> records =
                getRecordsByTestRunId(testRunId);

        if (records.isEmpty()) {

            throw new IllegalArgumentException(
                    "No repeatability readings found for this test run");
        }

        if (records.size() < 5) {

            throw new IllegalArgumentException(
                    "Repeatability test requires exactly 5 readings");
        }

        records.sort(
                Comparator.comparing(
                        RepeatabilityRecord::getReadingNumber));

        RepeatabilityRecord firstRecord =
                records.get(0);

        Long inspectionId =
                firstRecord.getInspectionId();

        Double referenceWeight =
                firstRecord.getReferenceWeight();

        List<Double> readings =
                new ArrayList<>();

        double sum = 0.0;

        double min = Double.MAX_VALUE;

        double max = -Double.MAX_VALUE;

        for (RepeatabilityRecord record : records) {

            double observed =
                    record.getObservedWeight();

            readings.add(observed);

            sum += observed;

            if (observed < min) {
                min = observed;
            }

            if (observed > max) {
                max = observed;
            }
        }

        double average =
                sum / records.size();

        double averageError =
                average - referenceWeight;

        double range =
                max - min;

        // -----------------------------------------
        // GET INSPECTION
        // -----------------------------------------

        Inspection inspection =
                inspectionRepository.findById(inspectionId)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        // -----------------------------------------
        // GET INSTRUMENT USING instrumentId
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
                calculateRepeatabilityMpe(
                        referenceWeight,
                        scaleInterval,
                        instrumentClass);

        // -----------------------------------------
        // RESULT
        // -----------------------------------------

        String result;

        if (range <= mpe) {
            result = "PASS";
        } else {
            result = "FAIL";
        }

        // -----------------------------------------
        // ROUND VALUES
        // -----------------------------------------

        average =
                Math.round(average * 10000.0) / 10000.0;

        averageError =
                Math.round(averageError * 10000.0) / 10000.0;

        range =
                Math.round(range * 10000.0) / 10000.0;

        // -----------------------------------------
        // RETURN SUMMARY
        // -----------------------------------------

        return new RepeatabilitySummaryResponse(
                testRunId,
                inspectionId,
                referenceWeight,
                readings,
                average,
                averageError,
                range,
                result
        );
    }

    // -----------------------------------------
    // MPE CALCULATION
    // -----------------------------------------

    private double calculateRepeatabilityMpe(
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
}