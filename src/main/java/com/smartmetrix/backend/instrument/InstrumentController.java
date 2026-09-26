package com.smartmetrix.backend.instrument;

import com.smartmetrix.backend.instrument.dto.CreateInstrumentRequest;
import com.smartmetrix.backend.instrument.dto.InstrumentHealthResponse;
import com.smartmetrix.backend.instrument.dto.UpdateInstrumentRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final InstrumentHealthService instrumentHealthService;

    public InstrumentController(
            InstrumentService instrumentService,
            InstrumentHealthService instrumentHealthService) {

        this.instrumentService = instrumentService;
        this.instrumentHealthService = instrumentHealthService;
    }

    // =========================
    // CREATE INSTRUMENT
    // =========================

    @PostMapping
    public Instrument createInstrument(
            @Valid @RequestBody CreateInstrumentRequest request) {

        Instrument instrument = new Instrument();

        instrument.setSerialNumber(request.getSerialNumber());
        instrument.setManufacturer(request.getManufacturer());
        instrument.setModel(request.getModel());
        instrument.setInstrumentClass(request.getInstrumentClass());
        instrument.setCapacity(request.getCapacity());
        instrument.setScaleInterval(request.getScaleInterval());
        instrument.setMinCapacity(request.getMinCapacity());
        instrument.setStatus(request.getStatus());

        return instrumentService.saveInstrument(instrument);
    }

    // =========================
    // GET ALL INSTRUMENTS
    // =========================
    // Inspector + Senior Officer + Controller + Admin
    // can view instruments.

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'INSPECTOR',
                'SENIOR_OFFICER',
                'CONTROLLER',
                'ADMIN'
            )
            """)
    public List<Instrument> getAllInstruments() {

        return instrumentService.getAllInstruments();
    }

    // =========================
    // GET INSTRUMENT BY ID
    // =========================
    // Inspector + Senior Officer + Controller + Admin
    // can view a particular instrument.

    @GetMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
                'INSPECTOR',
                'SENIOR_OFFICER',
                'CONTROLLER',
                'ADMIN'
            )
            """)
    public Instrument getInstrumentById(
            @PathVariable Long id) {

        return instrumentService.getInstrumentById(id);
    }

    // =========================
    // GET INSTRUMENT HEALTH
    // =========================
    // Inspector + Senior Officer + Controller + Admin
    // can view instrument health score.

    @GetMapping("/{id}/health")
    @PreAuthorize("""
            hasAnyRole(
                'INSPECTOR',
                'SENIOR_OFFICER',
                'CONTROLLER',
                'ADMIN'
            )
            """)
    public InstrumentHealthResponse getInstrumentHealth(
            @PathVariable Long id) {

        return instrumentHealthService.getInstrumentHealth(id);
    }

    // =========================
    // UPDATE INSTRUMENT
    // =========================
    // Class-level security means:
    // Inspector + Admin only.

    @PutMapping("/{id}")
    public Instrument updateInstrument(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInstrumentRequest request) {

        return instrumentService.updateInstrument(id, request);
    }

    // =========================
    // DELETE INSTRUMENT
    // =========================
    // Inspector + Admin only.

    @DeleteMapping("/{id}")
    public void deleteInstrument(
            @PathVariable Long id) {

        instrumentService.deleteInstrument(id);
    }
}