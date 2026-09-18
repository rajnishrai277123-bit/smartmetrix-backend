package com.smartmetrix.backend.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EccentricityRecordRepository
        extends JpaRepository<EccentricityRecord, Long> {

    List<EccentricityRecord> findByInspectionId(Long inspectionId);
}