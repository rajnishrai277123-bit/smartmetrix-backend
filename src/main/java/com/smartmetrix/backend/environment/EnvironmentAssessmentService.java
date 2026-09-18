package com.smartmetrix.backend.environment;

import org.springframework.stereotype.Service;

@Service
public class EnvironmentAssessmentService {

    public String assessTemperature(Double temperature) {

        if (temperature == null) {
            return "UNKNOWN";
        }

        // Prototype threshold - configurable later
        if (temperature < 10 || temperature > 35) {
            return "OUT_OF_RANGE";
        }

        if (temperature < 15 || temperature > 30) {
            return "WARNING";
        }

        return "NORMAL";
    }

    public String assessHumidity(Double humidity) {

        if (humidity == null) {
            return "UNKNOWN";
        }

        // Prototype threshold - configurable later
        if (humidity < 20 || humidity > 80) {
            return "OUT_OF_RANGE";
        }

        if (humidity < 30 || humidity > 70) {
            return "WARNING";
        }

        return "NORMAL";
    }

    public String assessVibration(Double vibration) {

        if (vibration == null) {
            return "UNKNOWN";
        }

        // Prototype threshold - configurable later
        if (vibration > 1.0) {
            return "OUT_OF_RANGE";
        }

        if (vibration > 0.5) {
            return "WARNING";
        }

        return "NORMAL";
    }

    public String assessOverall(
            Double temperature,
            Double humidity,
            Double vibration) {

        String temperatureStatus = assessTemperature(temperature);
        String humidityStatus = assessHumidity(humidity);
        String vibrationStatus = assessVibration(vibration);

        if ("OUT_OF_RANGE".equals(temperatureStatus)
                || "OUT_OF_RANGE".equals(humidityStatus)
                || "OUT_OF_RANGE".equals(vibrationStatus)) {

            return "OUT_OF_RANGE";
        }

        if ("WARNING".equals(temperatureStatus)
                || "WARNING".equals(humidityStatus)
                || "WARNING".equals(vibrationStatus)) {

            return "WARNING";
        }

        if ("UNKNOWN".equals(temperatureStatus)
                || "UNKNOWN".equals(humidityStatus)
                || "UNKNOWN".equals(vibrationStatus)) {

            return "UNKNOWN";
        }

        return "NORMAL";
    }
}