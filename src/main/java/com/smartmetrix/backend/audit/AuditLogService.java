package com.smartmetrix.backend.audit;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog createLog(
            Long userId,
            String action,
            String entity,
            Long entityId,
            String oldValue,
            String newValue) {

        AuditLog auditLog = new AuditLog();

        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setTimestamp(
                LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
        );

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getLogsByEntity(
            String entity,
            Long entityId) {

        return auditLogRepository
                .findByEntityAndEntityId(entity, entityId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}