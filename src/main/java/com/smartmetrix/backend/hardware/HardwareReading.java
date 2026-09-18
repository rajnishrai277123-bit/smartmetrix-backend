package com.smartmetrix.backend.hardware;

public class HardwareReading {

    private double weight;
    private boolean stable;
    private String source;

    public HardwareReading() {
    }

    public HardwareReading(
            double weight,
            boolean stable,
            String source) {

        this.weight = weight;
        this.stable = stable;
        this.source = source;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isStable() {
        return stable;
    }

    public String getSource() {
        return source;
    }
}