package com.smartmetrix.backend.environment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/environment")
public class EnvironmentRecordController {

    private final EnvironmentRecordService environmentRecordService;
    private final EnvironmentAssessmentService environmentAssessmentService;

    public EnvironmentRecordController(
            EnvironmentRecordService environmentRecordService,
            EnvironmentAssessmentService environmentAssessmentService) {

        this.environmentRecordService = environmentRecordService;
        this.environmentAssessmentService = environmentAssessmentService;
    }

    // Create environment record
    @PostMapping
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public EnvironmentRecord createRecord(
            @RequestBody EnvironmentRecord record) {

        return environmentRecordService.createRecord(record);
    }

    // Assess environment
    @PostMapping("/assess")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public Map<String, String> assessEnvironment(
            @RequestBody EnvironmentRecord record) {

        String temperatureStatus =
                environmentAssessmentService.assessTemperature(
                        record.getTemperature());

        String humidityStatus =
                environmentAssessmentService.assessHumidity(
                        record.getHumidity());

        String vibrationStatus =
                environmentAssessmentService.assessVibration(
                        record.getVibration());

        String overallStatus =
                environmentAssessmentService.assessOverall(
                        record.getTemperature(),
                        record.getHumidity(),
                        record.getVibration());

        Map<String, String> response = new HashMap<>();

        response.put("temperature", temperatureStatus);
        response.put("humidity", humidityStatus);
        response.put("vibration", vibrationStatus);
        response.put("overall", overallStatus);

        return response;
    }

    // Get all records
    @GetMapping
    @PreAuthorize("hasAnyRole('INSPECTOR', 'SENIOR_OFFICER', 'CONTROLLER', 'ADMIN')")
    public List<EnvironmentRecord> getAllRecords() {

        return environmentRecordService.getAllRecords();
    }

    // Get records by inspection
    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'SENIOR_OFFICER', 'CONTROLLER', 'ADMIN')")
    public List<EnvironmentRecord> getRecordsByInspectionId(
            @PathVariable Long inspectionId) {

        return environmentRecordService
                .getRecordsByInspectionId(inspectionId);
    }

    // Get one record
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'SENIOR_OFFICER', 'CONTROLLER', 'ADMIN')")
    public EnvironmentRecord getRecordById(
            @PathVariable Long id) {

        return environmentRecordService.getRecordById(id);
    }

    // Delete record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public void deleteRecord(
            @PathVariable Long id) {

        environmentRecordService.deleteRecord(id);
    }
}