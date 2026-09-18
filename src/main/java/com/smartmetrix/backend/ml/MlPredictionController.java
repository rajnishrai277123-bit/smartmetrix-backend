package com.smartmetrix.backend.ml;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;

    public MlPredictionController(
            MlPredictionService mlPredictionService) {

        this.mlPredictionService =
                mlPredictionService;
    }

    @GetMapping("/prediction/instrument/{instrumentId}")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_SENIOR_OFFICER', " +
                    "'ROLE_CONTROLLER', " +
                    "'ROLE_ADMIN')")
    public MlPredictionResponse predict(
            @PathVariable Long instrumentId) {

        return mlPredictionService.predict(
                instrumentId
        );
    }
}