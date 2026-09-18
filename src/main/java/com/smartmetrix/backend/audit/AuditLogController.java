package com.smartmetrix.backend.audit;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public AuditLog createLog(
            @RequestParam Long userId,
            @RequestParam String action,
            @RequestParam String entity,
            @RequestParam Long entityId,
            @RequestParam(required = false) String oldValue,
            @RequestParam(required = false) String newValue) {

        return auditLogService.createLog(
                userId,
                action,
                entity,
                entityId,
                oldValue,
                newValue
        );
    }

    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogService.getAllLogs();
    }

    @GetMapping("/{entity}/{entityId}")
    public List<AuditLog> getLogsByEntity(
            @PathVariable String entity,
            @PathVariable Long entityId) {

        return auditLogService.getLogsByEntity(
                entity,
                entityId
        );
    }
}