package com.smartmetrix.backend.virtualscale;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class VirtualScaleService {

    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;
    private final TestRecordService testRecordService;

    public VirtualScaleService(
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository,
            TestRecordService testRecordService) {

        this.inspectionRepository = inspectionRepository;
        this.instrumentRepository = instrumentRepository;
        this.testRecordService = testRecordService;
    }

    public VirtualScaleResponse simulate(
            VirtualScaleRequest request) {

        // -----------------------------
        // Basic validation
        // -----------------------------

        if (request.getInspectionId() == null) {
            throw new IllegalArgumentException(
                    "Inspection ID is required");
        }

        if (request.getReferenceWeight() == null
                || request.getReferenceWeight() < 0) {

            throw new IllegalArgumentException(
                    "Reference weight must be greater than or equal to zero");
        }

        // -----------------------------
        // Find inspection
        // -----------------------------

        Inspection inspection =
                inspectionRepository.findById(
                                request.getInspectionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Inspection not found"));

        // -----------------------------
        // Find instrument
        // -----------------------------

        Instrument instrument =
                instrumentRepository.findById(
                                inspection.getInstrumentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Instrument not found"));

        double referenceWeight =
                request.getReferenceWeight();

        double tareWeight =
                request.getTareWeight() != null
                        ? request.getTareWeight()
                        : 0.0;

        // -----------------------------
        // Validate tare
        // -----------------------------

        if (tareWeight < 0) {
            throw new IllegalArgumentException(
                    "Tare weight cannot be negative");
        }

        if (tareWeight >= referenceWeight
                && referenceWeight > 0) {

            throw new IllegalArgumentException(
                    "Tare weight must be less than reference weight");
        }

        // -----------------------------
        // Instrument information
        // -----------------------------

        double scaleInterval =
                instrument.getScaleInterval();

        double capacity =
                instrument.getCapacity();

        if (scaleInterval <= 0) {
            throw new IllegalArgumentException(
                    "Instrument scale interval must be greater than zero");
        }

        // -----------------------------
        // Capacity validation
        // -----------------------------

        if (referenceWeight > capacity) {
            throw new IllegalArgumentException(
                    "Reference weight cannot exceed instrument capacity");
        }

        // -----------------------------
        // Calculate net reference weight
        // -----------------------------

        double netReferenceWeight =
                referenceWeight - tareWeight;

        netReferenceWeight =
                Math.round(netReferenceWeight * 100000.0)
                        / 100000.0;

        // -----------------------------
        // Simulate machine variation
        // -----------------------------

        double variation =
                ThreadLocalRandom.current().nextDouble(
                        -scaleInterval,
                        scaleInterval
                );

        double observedWeight =
                netReferenceWeight + variation;

        // -----------------------------
        // Simulate scale resolution
        // -----------------------------

        observedWeight =
                Math.round(observedWeight / scaleInterval)
                        * scaleInterval;

        observedWeight =
                Math.round(observedWeight * 100000.0)
                        / 100000.0;

        // Prevent negative reading
        if (observedWeight < 0) {
            observedWeight = 0.0;
        }

        // -----------------------------
        // Return simulated reading
        // -----------------------------

        return new VirtualScaleResponse(
                inspection.getId(),
                instrument.getInstrumentClass(),
                capacity,
                scaleInterval,
                referenceWeight,
                tareWeight,
                netReferenceWeight,
                observedWeight,
                "STABLE"
        );
    }

    // -----------------------------------------
    // Capture simulated reading as TestRecord
    // -----------------------------------------

    public TestRecord capture(
            VirtualScaleRequest request) {

        VirtualScaleResponse reading =
                simulate(request);

        TestRecord testRecord =
                new TestRecord();

        testRecord.setInspectionId(
                reading.getInspectionId());

        testRecord.setTestType(
                "WEIGHING_PERFORMANCE");

        /*
         * For OIML calculation we use the
         * net reference weight.
         */
        testRecord.setReferenceWeight(
                reading.getNetReferenceWeight());

        testRecord.setObservedWeight(
                reading.getObservedWeight());

        if (request.getTestStage() != null
                && !request.getTestStage().isBlank()) {

            testRecord.setTestStage(
                    request.getTestStage()
            );

        } else {

            testRecord.setTestStage(
                    "INITIAL"
            );
        }

        /*
         * Existing TestRecordService will now:
         *
         * 1. Calculate error
         * 2. Calculate MPE
         * 3. Check compliance
         * 4. Generate PASS / FAIL
         * 5. Save into PostgreSQL
         */
        return testRecordService.createTestRecord(
                testRecord
        );
    }
}