package com.smartmetrix.backend.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repeatability")
public class RepeatabilityRecordController {

    private final RepeatabilityRecordService repeatabilityRecordService;

    public RepeatabilityRecordController(
            RepeatabilityRecordService repeatabilityRecordService) {

        this.repeatabilityRecordService =
                repeatabilityRecordService;
    }

    // -----------------------------------------
    // CREATE REPEATABILITY READING
    // -----------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public RepeatabilityRecord createRecord(
            @RequestBody RepeatabilityRecord record) {

        return repeatabilityRecordService
                .createRecord(record);
    }

    // -----------------------------------------
    // GET ALL RECORDS
    // -----------------------------------------

    @GetMapping
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public List<RepeatabilityRecord> getAllRecords() {

        return repeatabilityRecordService
                .getAllRecords();
    }

    // -----------------------------------------
    // GET RECORDS BY INSPECTION
    // -----------------------------------------

    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public List<RepeatabilityRecord> getRecordsByInspectionId(
            @PathVariable Long inspectionId) {

        return repeatabilityRecordService
                .getRecordsByInspectionId(inspectionId);
    }

    // -----------------------------------------
    // GET RECORDS BY TEST RUN
    // -----------------------------------------

    @GetMapping("/run/{testRunId}")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public List<RepeatabilityRecord> getRecordsByTestRunId(
            @PathVariable Long testRunId) {

        return repeatabilityRecordService
                .getRecordsByTestRunId(testRunId);
    }

    // -----------------------------------------
    // GET AVERAGE BY TEST RUN
    // -----------------------------------------

    @GetMapping("/run/{testRunId}/average")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public double calculateAverage(
            @PathVariable Long testRunId) {

        return repeatabilityRecordService
                .calculateAverage(testRunId);
    }

    // -----------------------------------------
    // GET RANGE BY TEST RUN
    // -----------------------------------------

    @GetMapping("/run/{testRunId}/range")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public double calculateRange(
            @PathVariable Long testRunId) {

        return repeatabilityRecordService
                .calculateRange(testRunId);
    }

    // -----------------------------------------
    // GET COMPLETE SUMMARY
    // -----------------------------------------

    @GetMapping("/run/{testRunId}/summary")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public RepeatabilitySummaryResponse getSummary(
            @PathVariable Long testRunId) {

        return repeatabilityRecordService
                .getSummary(testRunId);
    }
}