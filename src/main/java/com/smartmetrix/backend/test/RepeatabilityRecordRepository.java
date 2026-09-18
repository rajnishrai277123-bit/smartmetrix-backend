package com.smartmetrix.backend.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepeatabilityRecordRepository
        extends JpaRepository<RepeatabilityRecord, Long> {

    List<RepeatabilityRecord> findByInspectionId(Long inspectionId);

    List<RepeatabilityRecord> findByTestRunId(Long testRunId);
}