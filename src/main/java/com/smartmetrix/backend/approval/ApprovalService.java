package com.smartmetrix.backend.approval;

import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Approval createApproval(Approval approval) {

        inspectionRepository.findById(approval.getInspectionId())
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        return approvalRepository.save(approval);
    }

    public List<Approval> getApprovalsByInspectionId(
            Long inspectionId) {

        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        return approvalRepository
                .findByInspectionId(inspectionId);
    }
}