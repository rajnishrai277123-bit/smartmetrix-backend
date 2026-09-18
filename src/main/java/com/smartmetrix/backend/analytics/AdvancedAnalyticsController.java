package com.smartmetrix.backend.analytics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advanced-analytics")
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService advancedAnalyticsService;

    public AdvancedAnalyticsController(
            AdvancedAnalyticsService advancedAnalyticsService) {

        this.advancedAnalyticsService =
                advancedAnalyticsService;
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR'," +
                    "'ROLE_SENIOR_OFFICER'," +
                    "'ROLE_CONTROLLER'," +
                    "'ROLE_ADMIN')")
    public AdvancedAnalyticsResponse analyzeInstrument(
            @PathVariable Long instrumentId) {

        return advancedAnalyticsService
                .analyzeInstrument(instrumentId);
    }
}