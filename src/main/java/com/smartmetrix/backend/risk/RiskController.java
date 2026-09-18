package com.smartmetrix.backend.risk;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskService riskService;

    public RiskController(
            RiskService riskService) {

        this.riskService =
                riskService;
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_SENIOR_OFFICER', " +
                    "'ROLE_CONTROLLER', " +
                    "'ROLE_ADMIN')")
    public RiskResponse predictRisk(
            @PathVariable Long instrumentId) {

        return riskService.predictRisk(
                instrumentId
        );
    }
}