package com.smartmetrix.backend.hardware;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class VirtualWeighingMachine
        implements WeighingMachine {

    @Override
    public HardwareReading readWeight(
            double referenceWeight,
            double scaleInterval) {

        if (referenceWeight < 0) {
            throw new IllegalArgumentException(
                    "Reference weight cannot be negative");
        }

        if (scaleInterval <= 0) {
            throw new IllegalArgumentException(
                    "Scale interval must be greater than zero");
        }

        /*
         * Simulate a small measurement variation.
         */

        double variation =
                ThreadLocalRandom.current()
                        .nextDouble(
                                -scaleInterval,
                                scaleInterval
                        );

        double observedWeight =
                referenceWeight + variation;

        /*
         * Round the reading to the scale interval.
         */

        observedWeight =
                Math.round(
                        observedWeight / scaleInterval
                ) * scaleInterval;

        if (observedWeight < 0) {
            observedWeight = 0;
        }

        observedWeight =
                Math.round(
                        observedWeight * 100000.0
                ) / 100000.0;

        return new HardwareReading(
                observedWeight,
                true,
                "VIRTUAL_SCALE"
        );
    }

    @Override
    public String getDeviceName() {
        return "SmartMetrix Virtual Weighing Machine";
    }
}