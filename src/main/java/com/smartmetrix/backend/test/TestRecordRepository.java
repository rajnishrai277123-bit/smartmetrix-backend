package com.smartmetrix.backend.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRecordRepository
        extends JpaRepository<TestRecord, Long> {

    List<TestRecord> findByInspectionId(Long inspectionId);

    Optional<TestRecord> findByClientRecordId(
            String clientRecordId
    );
}