package com.smartmetrix.backend.certificate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/research-report")
public class ResearchReportController {

    private final ResearchReportPdfService reportService;

    public ResearchReportController(
            ResearchReportPdfService reportService) {

        this.reportService = reportService;
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR'," +
                    "'ROLE_SENIOR_OFFICER'," +
                    "'ROLE_CONTROLLER'," +
                    "'ROLE_ADMIN')")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable Long instrumentId) {

        byte[] pdf =
                reportService.generateResearchReport(
                        instrumentId
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=smartmetrix-research-report-"
                                + instrumentId
                                + ".pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}