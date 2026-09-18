package com.smartmetrix.backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityAndEntityId(
            String entity,
            Long entityId
    );
}