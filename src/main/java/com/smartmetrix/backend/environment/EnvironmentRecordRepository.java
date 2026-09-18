package com.smartmetrix.backend.environment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvironmentRecordRepository
        extends JpaRepository<EnvironmentRecord, Long> {

    List<EnvironmentRecord> findByInspectionId(Long inspectionId);
}