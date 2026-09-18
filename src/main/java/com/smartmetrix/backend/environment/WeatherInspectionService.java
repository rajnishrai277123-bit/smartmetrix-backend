package com.smartmetrix.backend.environment;

import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WeatherInspectionService {

    private final WeatherService weatherService;
    private final EnvironmentRecordService environmentRecordService;
    private final InspectionRepository inspectionRepository;

    public WeatherInspectionService(
            WeatherService weatherService,
            EnvironmentRecordService environmentRecordService,
            InspectionRepository inspectionRepository) {

        this.weatherService = weatherService;
        this.environmentRecordService = environmentRecordService;
        this.inspectionRepository = inspectionRepository;
    }

    public EnvironmentRecord captureWeatherForInspection(
            Long inspectionId,
            String location) {

        // Check inspection exists
        inspectionRepository.findById(inspectionId)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));

        // Fetch weather data
        WeatherResponse weather =
                weatherService.getWeather(location);

        // Create environment record
        EnvironmentRecord record =
                new EnvironmentRecord();

        record.setInspectionId(inspectionId);
        record.setTemperature(weather.getTemperature());
        record.setHumidity(weather.getHumidity());

        // Weather API vibration data provide nahi karta
        record.setVibration(null);

        record.setSource(weather.getSource());

        // Save + automatically assess environment
        return environmentRecordService.createRecord(record);
    }
}