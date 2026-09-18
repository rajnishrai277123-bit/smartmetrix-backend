package com.smartmetrix.backend.environment;

public class WeatherResponse {

    private String location;
    private Double temperature;
    private Double humidity;
    private String source;

    public WeatherResponse() {
    }

    public WeatherResponse(
            String location,
            Double temperature,
            Double humidity,
            String source) {

        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.source = source;
    }

    public String getLocation() {
        return location;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public String getSource() {
        return source;
    }
}