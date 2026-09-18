package com.smartmetrix.backend.analytics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService) {

        this.analyticsService =
                analyticsService;
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_SENIOR_OFFICER', " +
                    "'ROLE_CONTROLLER', " +
                    "'ROLE_ADMIN')")
    public AnalyticsResponse getAnalytics() {

        return analyticsService.getAnalytics();
    }
}