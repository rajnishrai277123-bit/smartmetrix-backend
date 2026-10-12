
package com.smartmetrix.backend.oiml;

import org.springframework.stereotype.Service;

@Service
public class OimlRuleEngine {

    public double calculateError(
            double referenceWeight,
            double observedWeight) {

        validateFinite(referenceWeight, "Reference weight");
        validateFinite(observedWeight, "Observed weight");

        if (referenceWeight < 0) {
            throw new IllegalArgumentException(
                    "Reference weight cannot be negative");
        }

        if (observedWeight < 0) {
            throw new IllegalArgumentException(
                    "Observed weight cannot be negative");
        }

        double error = observedWeight - referenceWeight;

        if (!Double.isFinite(error)) {
            throw new IllegalArgumentException(
                    "Calculated error is outside the supported range");
        }

        return Math.round(error * 100000.0) / 100000.0;
    }

    public double calculateMpe(
            String instrumentClass,
            double scaleInterval,
            double load) {

        return calculateMpe(
                instrumentClass,
                scaleInterval,
                load,
                OimlTestStage.INITIAL
        );
    }

    public double calculateMpe(
            String instrumentClass,
            double scaleInterval,
            double load,
            OimlTestStage testStage) {

        if (instrumentClass == null
                || instrumentClass.isBlank()) {
            throw new IllegalArgumentException(
                    "Instrument class is required");
        }

        validateFinite(scaleInterval, "Scale interval");
        validateFinite(load, "Load");

        if (scaleInterval <= 0) {
            throw new IllegalArgumentException(
                    "Scale interval must be greater than zero");
        }

        if (load < 0) {
            throw new IllegalArgumentException(
                    "Load cannot be negative");
        }

        if (testStage == null) {
            testStage = OimlTestStage.INITIAL;
        }

        String normalizedClass =
                instrumentClass.trim().toUpperCase();

        double verificationIntervals = load / scaleInterval;
        double mpeMultiplier;

        switch (normalizedClass) {

            case "I":
                if (verificationIntervals <= 50000) {
                    mpeMultiplier = 0.5;
                } else if (verificationIntervals <= 200000) {
                    mpeMultiplier = 1.0;
                } else {
                    mpeMultiplier = 1.5;
                }
                break;

            case "II":
                if (verificationIntervals <= 5000) {
                    mpeMultiplier = 0.5;
                } else if (verificationIntervals <= 20000) {
                    mpeMultiplier = 1.0;
                } else {
                    mpeMultiplier = 1.5;
                }
                break;

            case "III":
                if (verificationIntervals <= 500) {
                    mpeMultiplier = 0.5;
                } else if (verificationIntervals <= 2000) {
                    mpeMultiplier = 1.0;
                } else {
                    mpeMultiplier = 1.5;
                }
                break;

            case "IIII":
                if (verificationIntervals <= 50) {
                    mpeMultiplier = 0.5;
                } else if (verificationIntervals <= 200) {
                    mpeMultiplier = 1.0;
                } else {
                    mpeMultiplier = 1.5;
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported instrument class: "
                                + instrumentClass);
        }

        // Preserve the existing in-service behaviour.
        if (testStage == OimlTestStage.IN_SERVICE) {
            mpeMultiplier *= 2;
        }

        double mpe = mpeMultiplier * scaleInterval;

        if (!Double.isFinite(mpe)) {
            throw new IllegalArgumentException(
                    "Calculated MPE is outside the supported range");
        }

        return Math.round(mpe * 100000.0) / 100000.0;
    }

    public String checkCompliance(
            double error,
            double mpe) {

        validateFinite(error, "Error");
        validateFinite(mpe, "MPE");

        if (mpe < 0) {
            throw new IllegalArgumentException(
                    "MPE cannot be negative");
        }

        return Math.abs(error) <= mpe
                ? "PASS"
                : "FAIL";
    }

    // Additional method: explains the existing compliance result.
    public String getComplianceExplanation(
            double error,
            double mpe) {

        String result = checkCompliance(error, mpe);

        if ("PASS".equals(result)) {
            return "PASS: Absolute error "
                    + Math.abs(error)
                    + " is within the MPE limit of "
                    + mpe + ".";
        }

        return "FAIL: Absolute error "
                + Math.abs(error)
                + " exceeds the MPE limit of "
                + mpe + ".";
    }

    private void validateFinite(
            double value,
            String fieldName) {

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a finite number");
        }
    }
}
