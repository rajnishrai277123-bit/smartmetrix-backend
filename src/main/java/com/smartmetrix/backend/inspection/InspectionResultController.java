package com.smartmetrix.backend.inspection;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
public class InspectionResultController {

    private final InspectionResultService inspectionResultService;

    public InspectionResultController(
            InspectionResultService inspectionResultService) {

        this.inspectionResultService = inspectionResultService;
    }

    @PostMapping("/{inspectionId}/calculate-result")
    public Inspection calculateOverallResult(
            @PathVariable Long inspectionId) {

        return inspectionResultService
                .calculateOverallResult(inspectionId);
    }
}