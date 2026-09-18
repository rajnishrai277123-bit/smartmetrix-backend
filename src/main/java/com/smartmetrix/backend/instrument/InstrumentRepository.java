package com.smartmetrix.backend.instrument;

import org.springframework.data.jpa.repository.JpaRepository;
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);
}