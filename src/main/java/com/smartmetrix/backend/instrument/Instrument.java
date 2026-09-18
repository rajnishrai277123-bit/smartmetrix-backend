package com.smartmetrix.backend.instrument;

import jakarta.persistence.*;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String serialNumber;
    private String manufacturer;
    private String model;
    private String instrumentClass;
    private Double capacity;
    private Double scaleInterval;
    private Double minCapacity;
    private String status;

    public Instrument() {
    }

    public Long getId() {
        return id;
    }

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