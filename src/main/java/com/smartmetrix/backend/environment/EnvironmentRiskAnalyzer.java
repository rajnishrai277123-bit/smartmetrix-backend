
package com.smartmetrix.backend.environment;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnvironmentRiskAnalyzer {

    // Configurable research-prototype thresholds.
    // These are examples, not official instrument limits.
    private static final double MIN_TEMPERATURE = 10.0;
    private static final double MAX_TEMPERATURE = 35.0;

    private static final double MIN_HUMIDITY = 20.0;
    private static final double MAX_HUMIDITY = 80.0;

    private static final double MAX_VIBRATION = 0.5;

    public EnvironmentRisk assess(
            Double temperature,
            Double humidity,
            Double vibration) {

        List<String> riskFactors = new ArrayList<>();
        List<String> missingReadings = new ArrayList<>();

        if (temperature == null) {
            missingReadings.add("Temperature reading is missing");
        } else if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException(
                    "Temperature must be a finite number");
        } else if (temperature < MIN_TEMPERATURE
                || temperature > MAX_TEMPERATURE) {
            riskFactors.add(
                    "Temperature is outside the configured range of "
                            + MIN_TEMPERATURE + " to "
                            + MAX_TEMPERATURE + " °C");
        }

        if (humidity == null) {
            missingReadings.add("Humidity reading is missing");
        } else if (!Double.isFinite(humidity)
                || humidity < 0
                || humidity > 100) {
            throw new IllegalArgumentException(
                    "Humidity must be between 0 and 100 percent");
        } else if (humidity < MIN_HUMIDITY
                || humidity > MAX_HUMIDITY) {
            riskFactors.add(
                    "Humidity is outside the configured range of "
                            + MIN_HUMIDITY + " to "
                            + MAX_HUMIDITY + "%");
        }

        if (vibration == null) {
            missingReadings.add("Vibration reading is missing");
        } else if (!Double.isFinite(vibration) || vibration < 0) {
            throw new IllegalArgumentException(
                    "Vibration must be a finite, non-negative number");
        } else if (vibration > MAX_VIBRATION) {
            riskFactors.add(
                    "Vibration exceeds the configured threshold of "
                            + MAX_VIBRATION);
        }

        String riskLevel;

        if (!riskFactors.isEmpty()) {
            riskLevel = "HIGH";
        } else if (!missingReadings.isEmpty()) {
            riskLevel = "UNKNOWN";
        } else {
            riskLevel = "LOW";
        }

        String recommendation;

        if ("HIGH".equals(riskLevel)) {
            recommendation =
                    "Review the flagged readings and verify the instrument's "
                            + "specified operating limits before relying on "
                            + "the measurement.";
        } else if ("UNKNOWN".equals(riskLevel)) {
            recommendation =
                    "Collect the missing environmental readings before "
                            + "completing the environmental assessment.";
        } else {
            recommendation =
                    "All supplied readings are within the configured "
                            + "prototype thresholds.";
        }

        return new EnvironmentRisk(
                riskLevel,
                riskFactors,
                missingReadings,
                recommendation
        );
    }

    public static class EnvironmentRisk {

        private final String riskLevel;
        private final List<String> riskFactors;
        private final List<String> missingReadings;
        private final String recommendation;

        public EnvironmentRisk(
                String riskLevel,
                List<String> riskFactors,
                List<String> missingReadings,
                String recommendation) {
            this.riskLevel = riskLevel;
            this.riskFactors = riskFactors;
            this.missingReadings = missingReadings;
            this.recommendation = recommendation;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public List<String> getRiskFactors() {
            return riskFactors;
        }

        public List<String> getMissingReadings() {
            return missingReadings;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}
