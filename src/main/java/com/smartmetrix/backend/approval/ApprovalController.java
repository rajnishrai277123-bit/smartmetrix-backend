
package com.smartmetrix.backend.approval;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
            @Valid @RequestBody Approval approval,
            Authentication authentication) {

        Object credentials = authentication.getCredentials();

        if (!(credentials instanceof Long userId)) {
            throw new IllegalStateException(
                    "Authenticated user ID is unavailable"
            );
        }

        return approvalService.createApproval(approval, userId);
    }

    // Authorized roles can view approvals
    @GetMapping("/inspection/{inspectionId}")
    @PreAuthorize(
            "hasAnyRole('INSPECTOR', 'SENIOR_OFFICER', 'CONTROLLER', 'ADMIN')"
    )
    public List<Approval> getApprovalsByInspectionId(
            @PathVariable Long inspectionId) {

        return approvalService.getApprovalsByInspectionId(inspectionId);
    }
}
