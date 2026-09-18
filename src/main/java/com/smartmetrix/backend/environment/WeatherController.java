package com.smartmetrix.backend.environment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environment/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public WeatherResponse getWeather(
            @RequestParam String location) {

        return weatherService.getWeather(location);
    }
}