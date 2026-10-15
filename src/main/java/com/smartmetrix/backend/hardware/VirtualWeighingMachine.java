
package com.smartmetrix.backend.hardware;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class VirtualWeighingMachine implements WeighingMachine {

    // Number of simulated samples per weight capture
    private static final int SAMPLE_COUNT = 10;

    // Stability threshold relative to scale interval
    private static final double STABILITY_THRESHOLD = 2.0;

    @Override
    public HardwareReading readWeight(
            double referenceWeight,
            double scaleInterval) {

        if (!Double.isFinite(referenceWeight)
                || referenceWeight < 0) {
            throw new IllegalArgumentException(
                    "Reference weight must be finite and non-negative");
        }

        if (!Double.isFinite(scaleInterval)
                || scaleInterval <= 0) {
            throw new IllegalArgumentException(
                    "Scale interval must be finite and greater than zero");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        double totalWeight = 0.0;
        double minimumWeight = Double.POSITIVE_INFINITY;
        double maximumWeight = Double.NEGATIVE_INFINITY;

        // Generate multiple simulated measurements
        for (int i = 0; i < SAMPLE_COUNT; i++) {

            double variation = random.nextDouble(
                    -scaleInterval,
                    scaleInterval
            );

            double sample = referenceWeight + variation;

            // Round each simulated sample to the scale interval
            sample = Math.round(sample / scaleInterval)
                    * scaleInterval;

            sample = Math.max(0.0, sample);

            totalWeight += sample;
            minimumWeight = Math.min(minimumWeight, sample);
            maximumWeight = Math.max(maximumWeight, sample);
        }

        // Calculate the average of the simulated samples
        double observedWeight = totalWeight / SAMPLE_COUNT;

        // Round the final result to the scale interval
        observedWeight = Math.round(
                observedWeight / scaleInterval
        ) * scaleInterval;

        observedWeight = Math.max(0.0, observedWeight);

        // Avoid floating-point display noise
        observedWeight = Math.round(
                observedWeight * 100000.0
        ) / 100000.0;

        // Check the spread between minimum and maximum samples
        double spread = maximumWeight - minimumWeight;

        boolean stable = spread
                <= scaleInterval * STABILITY_THRESHOLD;

        return new HardwareReading(
                observedWeight,
                stable,
                "VIRTUAL_SCALE"
        );
    }

    @Override
    public String getDeviceName() {
        return "SmartMetrix Virtual Weighing Machine";
    }
}
