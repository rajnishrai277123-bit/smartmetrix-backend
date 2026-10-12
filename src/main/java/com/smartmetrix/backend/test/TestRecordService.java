
package com.smartmetrix.backend.test;

import com.smartmetrix.backend.environment.EnvironmentRiskAnalyzer;
import com.smartmetrix.backend.environment.EnvironmentRiskAnalyzer.EnvironmentRisk;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.oiml.OimlRuleEngine;
import com.smartmetrix.backend.oiml.OimlTestStage;
import com.smartmetrix.backend.test.exception.TestRecordNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TestRecordService {

    private final TestRecordRepository testRecordRepository;
    private final OimlRuleEngine oimlRuleEngine;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;
    private final EnvironmentRiskAnalyzer environmentRiskAnalyzer;

    public TestRecordService(
            TestRecordRepository testRecordRepository,
            InspectionRepository inspectionRepository,
            OimlRuleEngine oimlRuleEngine,
            InstrumentRepository instrumentRepository,
            EnvironmentRiskAnalyzer environmentRiskAnalyzer) {

        this.testRecordRepository = testRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.oimlRuleEngine = oimlRuleEngine;
        this.instrumentRepository = instrumentRepository;
        this.environmentRiskAnalyzer = environmentRiskAnalyzer;
    }

    @Transactional(readOnly = true)
    public List<TestRecord> getAllTestRecords() {
        List<TestRecord> records = testRecordRepository.findAll();

        records.forEach(this::applyEnvironmentAssessment);

        return records;
    }

    @Transactional(readOnly = true)
    public TestRecord getTestRecordById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Test record ID must be a positive number");
        }

        TestRecord record = testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException(
                                "Test record not found"));

        // Also calculate the assessment for older records.
        applyEnvironmentAssessment(record);

        return record;
    }

    public TestRecord createTestRecord(TestRecord testRecord) {
        if (testRecord == null) {
            throw new IllegalArgumentException(
                    "Test record request cannot be empty");
        }

        validateWeights(testRecord);

        validateEnvironment(
                testRecord.getTemperature(),
                testRecord.getHumidity(),
                testRecord.getVibration()
        );

        Inspection inspection =
                findInspection(testRecord.getInspectionId());

        Instrument instrument = findInstrument(inspection);
        OimlTestStage stage = resolveTestStage(testRecord.getTestStage());

        double error = oimlRuleEngine.calculateError(
                testRecord.getReferenceWeight(),
                testRecord.getObservedWeight()
        );

        double mpe = oimlRuleEngine.calculateMpe(
                instrument.getInstrumentClass(),
                instrument.getScaleInterval(),
                testRecord.getObservedWeight(),
                stage
        );

        String result = oimlRuleEngine.checkCompliance(error, mpe);

        String explanation =
                oimlRuleEngine.getComplianceExplanation(error, mpe);

        testRecord.setTestStage(stage.name());
        testRecord.setError(error);
        testRecord.setMpe(mpe);
        testRecord.setResult(result);
        testRecord.setComplianceExplanation(explanation);

        applyEnvironmentAssessment(testRecord);

        return testRecordRepository.save(testRecord);
    }

    public TestRecord updateTestRecord(
            Long id, TestRecord testRecord) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Test record ID must be a positive number");
        }

        if (testRecord == null) {
            throw new IllegalArgumentException(
                    "Test record request cannot be empty");
        }

        TestRecord existingRecord = testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException(
                                "Test record not found"));

        validateWeights(testRecord);

        validateEnvironment(
                testRecord.getTemperature(),
                testRecord.getHumidity(),
                testRecord.getVibration()
        );

        Inspection inspection =
                findInspection(testRecord.getInspectionId());

        Instrument instrument = findInstrument(inspection);
        OimlTestStage stage = resolveTestStage(testRecord.getTestStage());

        double error = oimlRuleEngine.calculateError(
                testRecord.getReferenceWeight(),
                testRecord.getObservedWeight()
        );

        double mpe = oimlRuleEngine.calculateMpe(
                instrument.getInstrumentClass(),
                instrument.getScaleInterval(),
                testRecord.getObservedWeight(),
                stage
        );

        String result = oimlRuleEngine.checkCompliance(error, mpe);

        String explanation =
                oimlRuleEngine.getComplianceExplanation(error, mpe);

        existingRecord.setInspectionId(testRecord.getInspectionId());
        existingRecord.setTestType(testRecord.getTestType());
        existingRecord.setReferenceWeight(testRecord.getReferenceWeight());
        existingRecord.setObservedWeight(testRecord.getObservedWeight());
        existingRecord.setTestStage(stage.name());
        existingRecord.setError(error);
        existingRecord.setMpe(mpe);
        existingRecord.setTemperature(testRecord.getTemperature());
        existingRecord.setHumidity(testRecord.getHumidity());
        existingRecord.setVibration(testRecord.getVibration());
        existingRecord.setResult(result);
        existingRecord.setComplianceExplanation(explanation);

        applyEnvironmentAssessment(existingRecord);

        return testRecordRepository.save(existingRecord);
    }

    public void deleteTestRecord(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Test record ID must be a positive number");
        }

        TestRecord testRecord = testRecordRepository.findById(id)
                .orElseThrow(() ->
                        new TestRecordNotFoundException(
                                "Test record not found"));

        testRecordRepository.delete(testRecord);
    }

    private void applyEnvironmentAssessment(TestRecord record) {
        EnvironmentRisk risk = environmentRiskAnalyzer.assess(
                record.getTemperature(),
                record.getHumidity(),
                record.getVibration()
        );

        record.setEnvironmentRiskLevel(risk.getRiskLevel());

        List<String> explanations = new ArrayList<>();

        if (!risk.getRiskFactors().isEmpty()) {
            explanations.add(
                    "Risk factors: "
                            + String.join("; ", risk.getRiskFactors())
            );
        }

        if (!risk.getMissingReadings().isEmpty()) {
            explanations.add(
                    "Missing readings: "
                            + String.join("; ", risk.getMissingReadings())
            );
        }

        if (explanations.isEmpty()) {
            explanations.add(
                    "No configured environmental threshold was exceeded."
            );
        }

        record.setEnvironmentRiskExplanation(
                String.join(" | ", explanations)
        );

        record.setEnvironmentRecommendation(
                risk.getRecommendation()
        );
    }

    private Inspection findInspection(Long inspectionId) {
        if (inspectionId == null || inspectionId <= 0) {
            throw new IllegalArgumentException(
                    "A valid inspection ID is required");
        }

        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));
    }

    private Instrument findInstrument(Inspection inspection) {
        if (inspection.getInstrumentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Instrument not associated with this inspection"
            );
        }

        return instrumentRepository.findById(
                        inspection.getInstrumentId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Instrument not found"
                        ));
    }

    private OimlTestStage resolveTestStage(String value) {
        if (value == null || value.isBlank()) {
            return OimlTestStage.INITIAL;
        }

        try {
            return OimlTestStage.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid test stage. Allowed values: "
                            + String.join(", ", getAllowedStages())
            );
        }
    }

    private String[] getAllowedStages() {
        return new String[]{
                OimlTestStage.INITIAL.name(),
                OimlTestStage.IN_SERVICE.name()
        };
    }

    private void validateWeights(TestRecord record) {
        Double referenceWeight = record.getReferenceWeight();
        Double observedWeight = record.getObservedWeight();

        if (referenceWeight == null
                || !Double.isFinite(referenceWeight)
                || referenceWeight < 0) {
            throw new IllegalArgumentException(
                    "Reference weight must be a finite, non-negative number");
        }

        if (observedWeight == null
                || !Double.isFinite(observedWeight)
                || observedWeight < 0) {
            throw new IllegalArgumentException(
                    "Observed weight must be a finite, non-negative number");
        }
    }

    private void validateEnvironment(
            Double temperature,
            Double humidity,
            Double vibration) {

        if (temperature != null && !Double.isFinite(temperature)) {
            throw new IllegalArgumentException(
                    "Temperature must be a finite number");
        }

        if (humidity != null) {
            if (!Double.isFinite(humidity)
                    || humidity < 0
                    || humidity > 100) {
                throw new IllegalArgumentException(
                        "Humidity must be between 0 and 100 percent");
            }
        }

        if (vibration != null) {
            if (!Double.isFinite(vibration) || vibration < 0) {
                throw new IllegalArgumentException(
                        "Vibration must be a finite, non-negative number");
            }
        }
    }
}
