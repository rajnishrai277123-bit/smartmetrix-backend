package com.smartmetrix.backend.oiml;

import org.springframework.stereotype.Service;

@Service
public class OimlRuleEngine {

    public double calculateError(
            double referenceWeight,
            double observedWeight) {

        double error = observedWeight - referenceWeight;

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

        String instrumentClassNormalized =
                instrumentClass.trim().toUpperCase();

        double verificationIntervals =
                load / scaleInterval;

        double mpeMultiplier;

        switch (instrumentClassNormalized) {

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

        // In-service MPE is twice the initial verification MPE
        if (testStage == OimlTestStage.IN_SERVICE) {
            mpeMultiplier = mpeMultiplier * 2;
        }

        double mpe = mpeMultiplier * scaleInterval;

        return Math.round(mpe * 100000.0) / 100000.0;
    }

    public String checkCompliance(
            double error,
            double mpe) {

        if (mpe < 0) {
            throw new IllegalArgumentException(
                    "MPE cannot be negative");
        }

        return Math.abs(error) <= mpe
                ? "PASS"
                : "FAIL";
    }
}