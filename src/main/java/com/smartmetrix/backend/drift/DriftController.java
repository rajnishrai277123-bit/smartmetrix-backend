package com.smartmetrix.backend.drift;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drift")
public class DriftController {

    private final DriftService driftService;

    public DriftController(
            DriftService driftService) {

        this.driftService =
                driftService;
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_SENIOR_OFFICER', " +
                    "'ROLE_CONTROLLER', " +
                    "'ROLE_ADMIN')")
    public DriftAnalysisResponse analyzeInstrument(
            @PathVariable Long instrumentId) {

        return driftService.analyzeInstrument(
                instrumentId
        );
    }
}