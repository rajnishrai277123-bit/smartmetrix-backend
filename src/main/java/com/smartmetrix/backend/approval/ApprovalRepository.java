package com.smartmetrix.backend.approval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository
        extends JpaRepository<Approval, Long> {

    List<Approval> findByInspectionId(Long inspectionId);
}