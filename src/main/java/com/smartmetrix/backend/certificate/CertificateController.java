package com.smartmetrix.backend.certificate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificatePdfService certificatePdfService;

    public CertificateController(
            CertificateService certificateService,
            CertificatePdfService certificatePdfService) {

        this.certificateService = certificateService;
        this.certificatePdfService = certificatePdfService;
    }

    // Only Controller or Admin can generate certificate
    @PostMapping("/inspection/{inspectionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public Certificate generateCertificate(
            @PathVariable Long inspectionId) {

        return certificateService.generateCertificate(inspectionId);
    }

    // Authorized roles can view certificates
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public List<Certificate> getAllCertificates() {

        return certificateService.getAllCertificates();
    }

    // Authorized roles can view certificate by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public Certificate getCertificateById(
            @PathVariable Long id) {

        return certificateService.getCertificateById(id);
    }

    // Authorized roles can download PDF
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public ResponseEntity<byte[]> downloadCertificatePdf(
            @PathVariable Long id) {

        byte[] pdf = certificatePdfService.generatePdf(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate-" + id + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{certificateId}/regenerate-hash")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public Certificate regenerateHash(
            @PathVariable Long certificateId) {

        return certificateService.regenerateHash(certificateId);
    }
}