package com.smartmetrix.backend.certificate;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verify")
public class CertificateVerificationController {

    private final CertificateVerificationService verificationService;

    public CertificateVerificationController(
            CertificateVerificationService verificationService) {

        this.verificationService = verificationService;
    }

    @GetMapping("/{certificateNumber}")
    public Map<String, Object> verifyCertificate(
            @PathVariable String certificateNumber) {

        return verificationService.verifyCertificate(certificateNumber);
    }
}