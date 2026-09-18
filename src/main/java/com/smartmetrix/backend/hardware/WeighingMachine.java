package com.smartmetrix.backend.hardware;

public interface WeighingMachine {

    HardwareReading readWeight(
            double referenceWeight,
            double scaleInterval);

    String getDeviceName();
}