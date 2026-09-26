package com.smartmetrix.backend.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRepository
        extends JpaRepository<Inspection, Long> {

    List<Inspection> findByInstrumentId(Long instrumentId);
}