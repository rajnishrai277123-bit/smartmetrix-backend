package com.smartmetrix.backend.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eccentricity")
public class EccentricityRecordController {

    private final EccentricityRecordService eccentricityRecordService;

    public EccentricityRecordController(
            EccentricityRecordService eccentricityRecordService) {

        this.eccentricityRecordService =
                eccentricityRecordService;
    }

    // -----------------------------------------
    // CREATE RECORD
    // -----------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public EccentricityRecord createRecord(
            @RequestBody EccentricityRecord record) {

        return eccentricityRecordService
                .createRecord(record);
    }

    // -----------------------------------------
    // GET ALL
    // -----------------------------------------

    @GetMapping
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public List<EccentricityRecord> getAllRecords() {

        return eccentricityRecordService
                .getAllRecords();
    }

    // -----------------------------------------
    // GET BY INSPECTION
    // -----------------------------------------

    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public List<EccentricityRecord> getRecordsByInspectionId(
            @PathVariable Long inspectionId) {

        return eccentricityRecordService
                .getRecordsByInspectionId(inspectionId);
    }

    // -----------------------------------------
    // MAXIMUM DIFFERENCE
    // -----------------------------------------

    @GetMapping("/inspection/{inspectionId}/difference")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public double calculateMaximumDifference(
            @PathVariable Long inspectionId) {

        return eccentricityRecordService
                .calculateMaximumDifference(
                        inspectionId);
    }

    // -----------------------------------------
    // COMPLETE SUMMARY
    // -----------------------------------------

    @GetMapping("/inspection/{inspectionId}/summary")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'INSPECTOR', " +
                    "'SENIOR_OFFICER', " +
                    "'CONTROLLER', " +
                    "'ADMIN')")
    public EccentricitySummaryResponse getSummary(
            @PathVariable Long inspectionId) {

        return eccentricityRecordService
                .getSummary(inspectionId);
    }
}