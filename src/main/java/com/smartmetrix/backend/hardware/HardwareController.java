package com.smartmetrix.backend.hardware;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hardware")
public class HardwareController {

    private final HardwareService hardwareService;

    public HardwareController(
            HardwareService hardwareService) {

        this.hardwareService =
                hardwareService;
    }

    @GetMapping("/device")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_SENIOR_OFFICER', " +
                    "'ROLE_CONTROLLER', " +
                    "'ROLE_ADMIN')")
    public String getDevice() {

        return hardwareService.getDeviceName();
    }

    @PostMapping("/read")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_ADMIN')")
    public HardwareReading readWeight(
            @RequestParam double referenceWeight,
            @RequestParam double scaleInterval) {

        return hardwareService.captureWeight(
                referenceWeight,
                scaleInterval
        );
    }
}