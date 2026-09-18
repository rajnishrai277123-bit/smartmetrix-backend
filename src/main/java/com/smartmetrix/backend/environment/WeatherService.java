package com.smartmetrix.backend.environment;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WeatherService {

    private final RestClient restClient;

    public WeatherService() {
        this.restClient = RestClient.builder().build();
    }

    public WeatherResponse getWeather(String location) {

        // Step 1: Location ko latitude/longitude me convert karna
        Map<?, ?> geoResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("geocoding-api.open-meteo.com")
                        .path("/v1/search")
                        .queryParam("name", location)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(Map.class);

        if (geoResponse == null || geoResponse.get("results") == null) {
            throw new IllegalStateException(
                    "Location not found: " + location
            );
        }

        var results = (java.util.List<?>) geoResponse.get("results");

        if (results.isEmpty()) {
            throw new IllegalStateException(
                    "Location not found: " + location
            );
        }

        Map<?, ?> firstResult = (Map<?, ?>) results.get(0);

        Double latitude =
                ((Number) firstResult.get("latitude")).doubleValue();

        Double longitude =
                ((Number) firstResult.get("longitude")).doubleValue();

        String resolvedLocation =
                String.valueOf(firstResult.get("name"));

        // Step 2: Latitude/Longitude se current weather lena
        Map<?, ?> weatherResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.open-meteo.com")
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam(
                                "current",
                                "temperature_2m,relative_humidity_2m"
                        )
                        .build())
                .retrieve()
                .body(Map.class);

        if (weatherResponse == null
                || weatherResponse.get("current") == null) {

            throw new IllegalStateException(
                    "Weather data unavailable"
            );
        }

        Map<?, ?> current =
                (Map<?, ?>) weatherResponse.get("current");

        Double temperature =
                ((Number) current.get("temperature_2m"))
                        .doubleValue();

        Double humidity =
                ((Number) current.get("relative_humidity_2m"))
                        .doubleValue();

        return new WeatherResponse(
                resolvedLocation,
                temperature,
                humidity,
                "WEATHER_API"
        );
    }
}