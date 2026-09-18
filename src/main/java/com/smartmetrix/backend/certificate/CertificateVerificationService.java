package com.smartmetrix.backend.certificate;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateVerificationService {

    private final CertificateRepository certificateRepository;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;

    public CertificateVerificationService(
            CertificateRepository certificateRepository,
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository) {

        this.certificateRepository =
                certificateRepository;

        this.inspectionRepository =
                inspectionRepository;

        this.instrumentRepository =
                instrumentRepository;
    }

    public Map<String, Object> verifyCertificate(
            String certificateNumber) {

        Map<String, Object> response =
                new HashMap<>();

        // -------------------------------------------------
        // 1. FIND CERTIFICATE
        // -------------------------------------------------

        Certificate certificate =
                certificateRepository
                        .findByCertificateNumber(
                                certificateNumber
                        )
                        .orElse(null);

        if (certificate == null) {

            response.put(
                    "valid",
                    false
            );

            response.put(
                    "message",
                    "Certificate not found"
            );

            response.put(
                    "integrityVerified",
                    false
            );

            return response;
        }

        // -------------------------------------------------
        // 2. BASIC CERTIFICATE DETAILS
        // -------------------------------------------------

        response.put(
                "certificateId",
                certificate.getId()
        );

        response.put(
                "certificateNumber",
                certificate.getCertificateNumber()
        );

        response.put(
                "certificateStatus",
                certificate.getStatus()
        );

        response.put(
                "storedHash",
                certificate.getHash()
        );

        response.put(
                "signature",
                certificate.getSignature()
        );

        // -------------------------------------------------
        // 3. FIND LINKED INSPECTION
        // -------------------------------------------------

        Inspection inspection =
                inspectionRepository.findById(
                        certificate.getInspectionId()
                ).orElse(null);

        if (inspection == null) {

            response.put(
                    "valid",
                    false
            );

            response.put(
                    "message",
                    "Certificate exists, but linked inspection was not found."
            );

            response.put(
                    "integrityVerified",
                    false
            );

            return response;
        }

        // -------------------------------------------------
        // 4. INSPECTION DETAILS
        // -------------------------------------------------

        response.put(
                "inspectionId",
                inspection.getId()
        );

        response.put(
                "inspectionStatus",
                inspection.getStatus()
        );

        response.put(
                "overallResult",
                inspection.getOverallResult()
        );

        response.put(
                "inspectorId",
                inspection.getInspectorId()
        );

        // -------------------------------------------------
        // 5. FIND INSTRUMENT
        // -------------------------------------------------

        Instrument instrument =
                instrumentRepository.findById(
                        inspection.getInstrumentId()
                ).orElse(null);

        if (instrument != null) {

            response.put(
                    "instrumentId",
                    instrument.getId()
            );

            response.put(
                    "manufacturer",
                    instrument.getManufacturer()
            );

            response.put(
                    "model",
                    instrument.getModel()
            );

            response.put(
                    "serialNumber",
                    instrument.getSerialNumber()
            );

            response.put(
                    "instrumentClass",
                    instrument.getInstrumentClass()
            );

            response.put(
                    "capacity",
                    instrument.getCapacity()
            );

            response.put(
                    "scaleInterval",
                    instrument.getScaleInterval()
            );

            response.put(
                    "minCapacity",
                    instrument.getMinCapacity()
            );

            response.put(
                    "instrumentStatus",
                    instrument.getStatus()
            );
        }

        // -------------------------------------------------
        // 6. RECALCULATE SHA-256
        // -------------------------------------------------

        String certificateData =
                certificate.getCertificateNumber()
                        + "|"
                        + inspection.getId()
                        + "|"
                        + inspection.getOverallResult();

        String calculatedHash =
                generateSha256(
                        certificateData
                );

        response.put(
                "calculatedHash",
                calculatedHash
        );

        // -------------------------------------------------
        // 7. COMPARE HASHES
        // -------------------------------------------------

        boolean hashMatches =
                certificate.getHash() != null
                        && certificate.getHash()
                        .equalsIgnoreCase(
                                calculatedHash
                        );

        response.put(
                "integrityVerified",
                hashMatches
        );

        // -------------------------------------------------
        // 8. CHECK CERTIFICATE STATUS
        // -------------------------------------------------

        boolean certificateStatusValid =
                "ISSUED".equalsIgnoreCase(
                        certificate.getStatus()
                );

        response.put(
                "certificateStatusValid",
                certificateStatusValid
        );

        // -------------------------------------------------
        // 9. CHECK INSPECTION STATUS
        // -------------------------------------------------

        boolean inspectionStatusValid =
                Inspection.CONTROLLER_APPROVED.equals(
                        inspection.getStatus()
                );

        response.put(
                "inspectionStatusValid",
                inspectionStatusValid
        );

        // -------------------------------------------------
        // 10. CHECK FINAL RESULT
        // -------------------------------------------------

        boolean resultValid =
                "PASS".equalsIgnoreCase(
                        inspection.getOverallResult()
                );

        response.put(
                "resultValid",
                resultValid
        );

        // -------------------------------------------------
        // 11. FINAL VERIFICATION DECISION
        // -------------------------------------------------

        boolean valid =
                certificateStatusValid
                        && inspectionStatusValid
                        && resultValid
                        && hashMatches;

        response.put(
                "valid",
                valid
        );

        // -------------------------------------------------
        // 12. DETAILED MESSAGE
        // -------------------------------------------------

        if (valid) {

            response.put(
                    "message",
                    "Certificate is valid. The certificate is ISSUED, linked to a Controller Approved PASS inspection, and the SHA-256 integrity check passed."
            );

        } else if (!certificateStatusValid) {

            response.put(
                    "message",
                    "Certificate verification failed because the certificate is not in ISSUED status."
            );

        } else if (!inspectionStatusValid) {

            response.put(
                    "message",
                    "Certificate verification failed because the linked inspection is not Controller Approved."
            );

        } else if (!resultValid) {

            response.put(
                    "message",
                    "Certificate verification failed because the linked inspection does not have PASS as its final result."
            );

        } else if (!hashMatches) {

            response.put(
                    "message",
                    "Certificate integrity verification failed because the stored SHA-256 hash does not match the calculated SHA-256 hash."
            );

        } else {

            response.put(
                    "message",
                    "Certificate verification failed."
            );
        }

        return response;
    }

    // -----------------------------------------------------
    // SHA-256
    // -----------------------------------------------------

    private String generateSha256(
            String data) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hashBytes =
                    digest.digest(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hexString =
                    new StringBuilder();

            for (byte b : hashBytes) {

                String hex =
                        Integer.toHexString(
                                0xff & b
                        );

                if (hex.length() == 1) {

                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to calculate SHA-256",
                    e
            );
        }
    }
}