package com.smartmetrix.backend.inspection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    // Inspector can create inspection
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public Inspection createInspection(@RequestBody Inspection inspection) {
        return inspectionService.createInspection(inspection);
    }

    // Authorized roles can view inspections
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public List<Inspection> getAllInspections() {
        return inspectionService.getAllInspections();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public Inspection getInspectionById(@PathVariable Long id) {
        return inspectionService.getInspectionById(id);
    }

    // Inspector completes inspection
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public Inspection completeInspection(@PathVariable Long id) {
        return inspectionService.completeInspection(id);
    }

    // Inspector submits inspection
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public Inspection submitInspection(@PathVariable Long id) {
        return inspectionService.submitInspection(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_SENIOR_OFFICER', 'ROLE_ADMIN')")
    public Inspection approveInspection(@PathVariable Long id) {
        return inspectionService.approveInspection(id);
    }
    @GetMapping("/debug-authorities")
    public Object debugAuthorities(
            org.springframework.security.core.Authentication authentication) {

        return authentication.getAuthorities();
    }
    // Controller final approval
    @PostMapping("/{id}/controller-approve")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public Inspection controllerApproveInspection(
            @PathVariable Long id) {

        return inspectionService.controllerApproveInspection(id);
    }
}