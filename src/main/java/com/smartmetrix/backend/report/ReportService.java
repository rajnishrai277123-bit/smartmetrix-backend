package com.smartmetrix.backend.report;

import com.smartmetrix.backend.environment.EnvironmentRecord;
import com.smartmetrix.backend.environment.EnvironmentRecordRepository;
import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.test.EccentricityRecord;
import com.smartmetrix.backend.test.EccentricityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecord;
import com.smartmetrix.backend.test.RepeatabilityRecordRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;
    private final TestRecordRepository testRecordRepository;
    private final EnvironmentRecordRepository environmentRecordRepository;
    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final EccentricityRecordRepository eccentricityRecordRepository;

    public ReportService(
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository,
            TestRecordRepository testRecordRepository,
            EnvironmentRecordRepository environmentRecordRepository,
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            EccentricityRecordRepository eccentricityRecordRepository
    ) {
        this.inspectionRepository = inspectionRepository;
        this.instrumentRepository = instrumentRepository;
        this.testRecordRepository = testRecordRepository;
        this.environmentRecordRepository = environmentRecordRepository;
        this.repeatabilityRecordRepository = repeatabilityRecordRepository;
        this.eccentricityRecordRepository = eccentricityRecordRepository;
    }

    public InspectionReportResponse getInspectionReport(Long inspectionId) {

        // =========================
        // INSPECTION
        // =========================

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new RuntimeException("Inspection not found"));

        InspectionReportResponse response = new InspectionReportResponse();

        response.setInspectionId(inspection.getId());
        response.setInstrumentId(inspection.getInstrumentId());
        response.setInspectorId(inspection.getInspectorId());
        response.setInspectionStatus(inspection.getStatus());
        response.setOverallResult(inspection.getOverallResult());
        response.setCreatedAt(inspection.getCreatedAt());
        response.setCompletedAt(inspection.getCompletedAt());

        // =========================
        // INSTRUMENT
        // =========================

        Instrument instrument = instrumentRepository
                .findById(inspection.getInstrumentId())
                .orElse(null);

        if (instrument != null) {

            response.setInstrument(
                    new InspectionReportResponse.InstrumentReport(
                            instrument.getId(),
                            instrument.getSerialNumber(),
                            instrument.getManufacturer(),
                            instrument.getModel(),
                            instrument.getInstrumentClass(),
                            instrument.getCapacity(),
                            instrument.getScaleInterval(),
                            instrument.getMinCapacity(),
                            instrument.getStatus()
                    )
            );
        }

        // =========================
        // WEIGHING PERFORMANCE
        // =========================

        List<TestRecord> testRecords =
                testRecordRepository.findByInspectionId(inspectionId);

        List<InspectionReportResponse.TestRecordReport> testReports =
                testRecords.stream()
                        .map(test -> new InspectionReportResponse.TestRecordReport(
                                test.getId(),
                                test.getClientRecordId(),
                                test.getTestType(),
                                test.getReferenceWeight(),
                                test.getObservedWeight(),
                                test.getError(),
                                test.getMpe(),
                                test.getTemperature(),
                                test.getHumidity(),
                                test.getVibration(),
                                test.getResult(),
                                test.getTestStage(),
                                test.getCreatedAt()
                        ))
                        .toList();

        response.setTestRecords(testReports);

        // =========================
        // ENVIRONMENT
        // =========================

        List<EnvironmentRecord> environmentRecords =
                environmentRecordRepository.findByInspectionId(inspectionId);

        List<InspectionReportResponse.EnvironmentReport> environmentReports =
                environmentRecords.stream()
                        .map(environment -> new InspectionReportResponse.EnvironmentReport(
                                environment.getId(),
                                environment.getTemperature(),
                                environment.getHumidity(),
                                environment.getVibration(),
                                environment.getSource(),
                                environment.getStatus()
                        ))
                        .toList();

        response.setEnvironmentRecords(environmentReports);

        // =========================
        // REPEATABILITY
        // =========================

        List<RepeatabilityRecord> repeatabilityRecords =
                repeatabilityRecordRepository.findByInspectionId(inspectionId);

        List<InspectionReportResponse.RepeatabilityReport> repeatabilityReports =
                repeatabilityRecords.stream()
                        .map(record -> new InspectionReportResponse.RepeatabilityReport(
                                record.getId(),
                                record.getTestRunId(),
                                record.getReferenceWeight(),
                                record.getObservedWeight(),
                                record.getReadingNumber(),
                                record.getCreatedAt()
                        ))
                        .toList();

        response.setRepeatabilityRecords(repeatabilityReports);

        // =========================
        // ECCENTRICITY
        // =========================

        List<EccentricityRecord> eccentricityRecords =
                eccentricityRecordRepository.findByInspectionId(inspectionId);

        List<InspectionReportResponse.EccentricityReport> eccentricityReports =
                eccentricityRecords.stream()
                        .map(record -> new InspectionReportResponse.EccentricityReport(
                                record.getId(),
                                record.getPosition(),
                                record.getReferenceWeight(),
                                record.getObservedWeight(),
                                record.getCreatedAt()
                        ))
                        .toList();

        response.setEccentricityRecords(eccentricityReports);

        return response;
    }
}