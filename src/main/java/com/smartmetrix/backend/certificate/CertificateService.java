package com.smartmetrix.backend.certificate;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final InspectionRepository inspectionRepository;

    public CertificateService(
            CertificateRepository certificateRepository,
            InspectionRepository inspectionRepository) {

        this.certificateRepository = certificateRepository;
        this.inspectionRepository = inspectionRepository;
    }

    public Certificate generateCertificate(Long inspectionId) {

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException("Inspection not found"));

        // Certificate can be generated only after Controller approval
        if (!Inspection.CONTROLLER_APPROVED.equals(inspection.getStatus())) {
            throw new IllegalStateException(
                    "Certificate can be generated only after Controller approval");
        }

        // Certificate can be generated only for PASS inspections
        if (!Inspection.RESULT_PASS.equals(
                inspection.getOverallResult())) {

            throw new IllegalStateException(
                    "Certificate can be generated only for PASS inspections");
        }

        // Inspector ID is mandatory for certificate generation
        if (inspection.getInspectorId() == null) {

            throw new IllegalStateException(
                    "Certificate cannot be generated without Inspector ID");
        }

        // Prevent duplicate certificate
        if (certificateRepository.existsByInspectionId(inspectionId)) {
            throw new IllegalStateException(
                    "Certificate already exists for this inspection");
        }

        Certificate certificate = new Certificate();

        certificate.setInspectionId(inspectionId);

        String certificateNumber =
                String.format("SMX-2026-%05d", inspectionId);

        certificate.setCertificateNumber(certificateNumber);

        certificate.setStatus("ISSUED");

        // Generate SHA-256 hash
        String certificateData =
                certificateNumber + "|" +
                        inspection.getId() + "|" +
                        inspection.getOverallResult();

        certificate.setHash(generateSha256(certificateData));

        return certificateRepository.save(certificate);
    }

    private String generateSha256(String data) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString =
                    new StringBuilder();

            for (byte b : hashBytes) {

                String hex =
                        Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available", e);
        }
    }

    public List<Certificate> getAllCertificates() {

        return certificateRepository.findAll();
    }

    public Certificate getCertificateById(Long id) {

        return certificateRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Certificate not found"));
    }

    public Certificate regenerateHash(Long certificateId) {

        Certificate certificate =
                certificateRepository.findById(certificateId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Certificate not found"));

        Inspection inspection =
                inspectionRepository
                        .findById(certificate.getInspectionId())
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        String certificateData =
                certificate.getCertificateNumber() + "|" +
                        inspection.getId() + "|" +
                        inspection.getOverallResult();

        certificate.setHash(
                generateSha256(certificateData));

        return certificateRepository.save(certificate);
    }
}