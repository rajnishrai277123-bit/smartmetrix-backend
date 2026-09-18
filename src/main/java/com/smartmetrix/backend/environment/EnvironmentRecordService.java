package com.smartmetrix.backend.environment;

import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvironmentRecordService {

    private final EnvironmentRecordRepository environmentRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final EnvironmentAssessmentService environmentAssessmentService;

    public EnvironmentRecordService(
            EnvironmentRecordRepository environmentRecordRepository,
            InspectionRepository inspectionRepository,
            EnvironmentAssessmentService environmentAssessmentService) {

        this.environmentRecordRepository = environmentRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.environmentAssessmentService = environmentAssessmentService;
    }

    public EnvironmentRecord createRecord(EnvironmentRecord record) {

        // Check inspection exists
        inspectionRepository.findById(record.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException("Inspection not found"));

        // Default source
        if (record.getSource() == null
                || record.getSource().isBlank()) {

            record.setSource("MANUAL");
        }

        // Automatically assess environment
        String overallStatus =
                environmentAssessmentService.assessOverall(
                        record.getTemperature(),
                        record.getHumidity(),
                        record.getVibration()
                );

        record.setStatus(overallStatus);

        return environmentRecordRepository.save(record);
    }

    public List<EnvironmentRecord> getAllRecords() {

        return environmentRecordRepository.findAll();
    }

    public List<EnvironmentRecord> getRecordsByInspectionId(
            Long inspectionId) {

        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        return environmentRecordRepository
                .findByInspectionId(inspectionId);
    }

    public EnvironmentRecord getRecordById(Long id) {

        return environmentRecordRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Environment record not found"));
    }

    public void deleteRecord(Long id) {

        EnvironmentRecord record =
                environmentRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Environment record not found"));

        environmentRecordRepository.delete(record);
    }
}