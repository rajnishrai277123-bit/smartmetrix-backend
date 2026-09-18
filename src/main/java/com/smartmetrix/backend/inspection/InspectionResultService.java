package com.smartmetrix.backend.inspection;

import com.smartmetrix.backend.test.RepeatabilityRecordService;
import com.smartmetrix.backend.test.EccentricityRecordService;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;

@Service
public class InspectionResultService {

    private final InspectionRepository inspectionRepository;
    private final TestRecordRepository testRecordRepository;
    private final RepeatabilityRecordService repeatabilityRecordService;
    private final EccentricityRecordService eccentricityRecordService;

    public InspectionResultService(
            InspectionRepository inspectionRepository,
            TestRecordRepository testRecordRepository,
            RepeatabilityRecordService repeatabilityRecordService,
            EccentricityRecordService eccentricityRecordService) {

        this.inspectionRepository = inspectionRepository;
        this.testRecordRepository = testRecordRepository;
        this.repeatabilityRecordService = repeatabilityRecordService;
        this.eccentricityRecordService = eccentricityRecordService;
    }

    public Inspection calculateOverallResult(Long inspectionId) {

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        boolean weighingPerformanceFailed =
                testRecordRepository.findAll()
                        .stream()
                        .filter(record ->
                                record.getInspectionId().equals(inspectionId))
                        .anyMatch(record ->
                                "FAIL".equals(record.getResult()));

        boolean hasRepeatabilityData =
                !repeatabilityRecordService
                        .getRecordsByInspectionId(inspectionId)
                        .isEmpty();

        boolean hasEccentricityData =
                !eccentricityRecordService
                        .getRecordsByInspectionId(inspectionId)
                        .isEmpty();

        if (weighingPerformanceFailed) {

            inspection.setOverallResult(Inspection.RESULT_FAIL);

        } else if (!hasRepeatabilityData || !hasEccentricityData) {

            inspection.setOverallResult(Inspection.RESULT_PENDING);

        } else {

            inspection.setOverallResult(Inspection.RESULT_PASS);
        }

        return inspectionRepository.save(inspection);
    }
}