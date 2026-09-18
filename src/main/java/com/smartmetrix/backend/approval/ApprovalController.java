package com.smartmetrix.backend.approval;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    // Only Senior Officer or Admin can create an approval
    @PostMapping
    @PreAuthorize("hasAnyRole('SENIOR_OFFICER', 'ADMIN')")
    public Approval createApproval(
            @RequestBody Approval approval) {

        return approvalService.createApproval(approval);
    }

    // Authorized roles can view approvals
    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'SENIOR_OFFICER', 'CONTROLLER', 'ADMIN')")
    public List<Approval> getApprovalsByInspectionId(
            @PathVariable Long inspectionId) {

        return approvalService
                .getApprovalsByInspectionId(inspectionId);
    }
}