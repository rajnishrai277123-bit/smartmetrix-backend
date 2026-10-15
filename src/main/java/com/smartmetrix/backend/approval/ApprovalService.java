
package com.smartmetrix.backend.approval;

import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final InspectionRepository inspectionRepository;

    public ApprovalService(
            ApprovalRepository approvalRepository,
            InspectionRepository inspectionRepository) {

        this.approvalRepository = approvalRepository;
        this.inspectionRepository = inspectionRepository;
    }

    @Transactional
    public Approval createApproval(Approval approval, Long authenticatedUserId) {

        if (authenticatedUserId == null) {
            throw new IllegalArgumentException(
                    "Authenticated user ID is required"
            );
        }

        if (approval.getInspectionId() == null) {
            throw new IllegalArgumentException(
                    "Inspection ID is required"
            );
        }

        inspectionRepository.findById(approval.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"
                        ));

        if (approval.getAction() == null
                || approval.getAction().isBlank()) {
            throw new IllegalArgumentException(
                    "Action is required"
            );
        }

        String action = approval.getAction()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!action.equals("APPROVE") && !action.equals("REJECT")) {
            throw new IllegalArgumentException(
                    "Action must be APPROVE or REJECT"
            );
        }

        if (approval.getRemarks() == null
                || approval.getRemarks().isBlank()) {
            throw new IllegalArgumentException(
                    "Remarks are required"
            );
        }

        if (approval.getRemarks().length() > 1000) {
            throw new IllegalArgumentException(
                    "Remarks cannot exceed 1000 characters"
            );
        }

        // Never trust the user ID supplied by the client.
        approval.setUserId(authenticatedUserId);
        approval.setAction(action);
        approval.setRemarks(approval.getRemarks().trim());

        // Do not accept a client-supplied database ID.
        // The database will generate the approval ID.
        return approvalRepository.save(approval);
    }

    @Transactional(readOnly = true)
    public List<Approval> getApprovalsByInspectionId(Long inspectionId) {

        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"
                        ));

        return approvalRepository.findByInspectionId(inspectionId);
    }
}
