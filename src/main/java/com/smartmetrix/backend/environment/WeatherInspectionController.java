package com.smartmetrix.backend.environment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environment/weather")
public class WeatherInspectionController {

    private final WeatherInspectionService weatherInspectionService;

    public WeatherInspectionController(
            WeatherInspectionService weatherInspectionService) {

        this.weatherInspectionService = weatherInspectionService;
    }

    @PostMapping("/inspection/{inspectionId}")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public EnvironmentRecord captureWeather(
            @PathVariable Long inspectionId,
            @RequestBody WeatherInspectionRequest request) {

        return weatherInspectionService.captureWeatherForInspection(
                inspectionId,
                request.getLocation()
        );
    }
}