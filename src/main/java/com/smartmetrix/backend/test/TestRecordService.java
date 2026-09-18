package com.smartmetrix.backend.test;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.oiml.OimlRuleEngine;
import com.smartmetrix.backend.oiml.OimlTestStage;
import com.smartmetrix.backend.test.exception.TestRecordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestRecordService {

    private final TestRecordRepository testRecordRepository;
    private final OimlRuleEngine oimlRuleEngine;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;

    public TestRecordService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository,
            OimlRuleEngine oimlRuleEngine,
            InstrumentRepository instrumentRepository) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.oimlRuleEngine = oimlRuleEngine;
        this.instrumentRepository = instrumentRepository;
    }

    public List<TestRecord> getAllTestRecords() {
        return testRecordRepository.findAll();
    }

    public TestRecord getTestRecordById(Long id) {

        return testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException("Test record not found"));
    }

    public TestRecord updateTestRecord(Long id, TestRecord testRecord) {

        TestRecord existingRecord = testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException("Test record not found"));

        Inspection inspection = inspectionRepository
                .findById(testRecord.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException("Inspection not found"));

        Instrument instrument = instrumentRepository
                .findById(inspection.getInstrumentId())
                .orElseThrow(() ->
                        new RuntimeException("Instrument not found"));

        existingRecord.setInspectionId(testRecord.getInspectionId());
        existingRecord.setTestType(testRecord.getTestType());
        existingRecord.setReferenceWeight(testRecord.getReferenceWeight());
        existingRecord.setObservedWeight(testRecord.getObservedWeight());

        // Determine OIML test stage
        OimlTestStage testStage = OimlTestStage.INITIAL;

        if (testRecord.getTestStage() != null
                && !testRecord.getTestStage().isBlank()) {

            testStage = OimlTestStage.valueOf(
                    testRecord.getTestStage().trim().toUpperCase()
            );
        }

        existingRecord.setTestStage(testStage.name());

        double error = oimlRuleEngine.calculateError(
                testRecord.getReferenceWeight(),
                testRecord.getObservedWeight()
        );

        existingRecord.setError(error);

        double mpe = oimlRuleEngine.calculateMpe(
                instrument.getInstrumentClass(),
                instrument.getScaleInterval(),
                testRecord.getObservedWeight(),
                testStage
        );

        existingRecord.setMpe(mpe);

        String result = oimlRuleEngine.checkCompliance(
                error,
                mpe
        );

        existingRecord.setTemperature(testRecord.getTemperature());
        existingRecord.setHumidity(testRecord.getHumidity());
        existingRecord.setVibration(testRecord.getVibration());
        existingRecord.setResult(result);

        return testRecordRepository.save(existingRecord);
    }

    public TestRecord createTestRecord(TestRecord testRecord) {

        Inspection inspection = inspectionRepository
                .findById(testRecord.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException("Inspection not found"));

        Instrument instrument = instrumentRepository
                .findById(inspection.getInstrumentId())
                .orElseThrow(() ->
                        new RuntimeException("Instrument not found"));

        // Determine OIML test stage
        OimlTestStage testStage = OimlTestStage.INITIAL;

        if (testRecord.getTestStage() != null
                && !testRecord.getTestStage().isBlank()) {

            testStage = OimlTestStage.valueOf(
                    testRecord.getTestStage().trim().toUpperCase()
            );
        }

        testRecord.setTestStage(testStage.name());

        double error = oimlRuleEngine.calculateError(
                testRecord.getReferenceWeight(),
                testRecord.getObservedWeight()
        );

        testRecord.setError(error);

        double mpe = oimlRuleEngine.calculateMpe(
                instrument.getInstrumentClass(),
                instrument.getScaleInterval(),
                testRecord.getObservedWeight(),
                testStage
        );

        testRecord.setMpe(mpe);

        String result = oimlRuleEngine.checkCompliance(
                error,
                mpe
        );

        testRecord.setResult(result);

        return testRecordRepository.save(testRecord);
    }

    public void deleteTestRecord(Long id) {

        TestRecord testRecord = testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException("Test record not found"));

        testRecordRepository.delete(testRecord);
    }
}