package com.smartmetrix.backend.instrument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateInstrumentRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Instrument class is required")
    private String instrumentClass;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Double capacity;

    @NotNull(message = "Scale interval is required")
    @Positive(message = "Scale interval must be greater than 0")
    private Double scaleInterval;

    @NotNull(message = "Minimum capacity is required")
    @Positive(message = "Minimum capacity must be greater than 0")
    private Double minCapacity;

    @NotBlank(message = "Status is required")
    private String status;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getInstrumentClass() {
        return instrumentClass;
    }

    public void setInstrumentClass(String instrumentClass) {
        this.instrumentClass = instrumentClass;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getScaleInterval() {
        return scaleInterval;
    }

    public void setScaleInterval(Double scaleInterval) {
        this.scaleInterval = scaleInterval;
    }

    public Double getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Double minCapacity) {
        this.minCapacity = minCapacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}