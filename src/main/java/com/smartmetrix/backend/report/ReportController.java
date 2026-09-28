package com.smartmetrix.backend.report;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inspection/{inspectionId}")
    public InspectionReportResponse getInspectionReport(
            @PathVariable Long inspectionId) {

        return reportService.getInspectionReport(inspectionId);
    }
}