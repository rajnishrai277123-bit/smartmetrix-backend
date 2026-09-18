package com.smartmetrix.backend.certificate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    boolean existsByInspectionId(Long inspectionId);

    boolean existsByCertificateNumber(String certificateNumber);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);
}