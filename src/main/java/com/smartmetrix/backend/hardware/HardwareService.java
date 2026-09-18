package com.smartmetrix.backend.hardware;

import org.springframework.stereotype.Service;

@Service
public class HardwareService {

    private final WeighingMachine weighingMachine;

    public HardwareService(
            VirtualWeighingMachine virtualWeighingMachine) {

        /*
         * Currently Virtual Scale is used.
         *
         * Later this can be replaced by:
         * ESP32WeighingMachine
         * SerialWeighingMachine
         * BluetoothWeighingMachine
         * etc.
         */

        this.weighingMachine =
                virtualWeighingMachine;
    }

    public HardwareReading captureWeight(
            double referenceWeight,
            double scaleInterval) {

        return weighingMachine.readWeight(
                referenceWeight,
                scaleInterval
        );
    }

    public String getDeviceName() {
        return weighingMachine.getDeviceName();
    }
}