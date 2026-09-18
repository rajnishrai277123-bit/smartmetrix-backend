package com.smartmetrix.backend.virtualscale;

import com.smartmetrix.backend.test.TestRecord;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/virtual-scale")
public class VirtualScaleController {

    private final VirtualScaleService virtualScaleService;

    public VirtualScaleController(
            VirtualScaleService virtualScaleService) {

        this.virtualScaleService =
                virtualScaleService;
    }

    // -----------------------------------------
    // Simulate weighing
    // -----------------------------------------

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public VirtualScaleResponse simulate(
            @RequestBody VirtualScaleRequest request) {

        return virtualScaleService.simulate(request);
    }

    // -----------------------------------------
    // Capture weighing as TestRecord
    // -----------------------------------------

    @PostMapping("/capture")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public TestRecord capture(
            @RequestBody VirtualScaleRequest request) {

        return virtualScaleService.capture(request);
    }
}