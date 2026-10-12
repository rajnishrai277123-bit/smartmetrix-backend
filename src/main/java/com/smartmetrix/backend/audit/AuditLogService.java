
package com.smartmetrix.backend.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuditLogService {

    private static final String GENESIS_HASH = "0".repeat(64);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Create a new audit log
    @Transactional
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

        List<AuditLog> logs =
                auditLogRepository.findAllByOrderByIdAsc();

        String previousHash = logs.isEmpty()
                ? GENESIS_HASH
                : logs.get(logs.size() - 1).getCurrentHash();

        if (previousHash == null) {
            throw new IllegalStateException(
                    "Previous audit log has no hash. "
                            + "Existing audit history must be migrated first."
            );
        }

        auditLog.setPreviousHash(previousHash);
        auditLog.setCurrentHash(calculateHash(auditLog));

        return auditLogRepository.save(auditLog);
    }

    // Migrate old audit logs only once
    @Transactional
    public int migrateLegacyLogs() {

        List<AuditLog> logs =
                auditLogRepository.findAllByOrderByIdAsc();

        if (logs.isEmpty()) {
            return 0;
        }

        boolean allUnmigrated = logs.stream()
                .allMatch(log ->
                        log.getPreviousHash() == null
                                && log.getCurrentHash() == null
                );

        if (!allUnmigrated) {
            throw new IllegalStateException(
                    "Migration stopped. Logs are already migrated "
                            + "or the hash chain is partially populated."
            );
        }

        String previousHash = GENESIS_HASH;

        for (AuditLog log : logs) {
            log.setPreviousHash(previousHash);

            String currentHash = calculateHash(log);
            log.setCurrentHash(currentHash);

            previousHash = currentHash;
        }

        auditLogRepository.saveAll(logs);

        return logs.size();
    }

    // Verify the complete audit hash chain
    @Transactional(readOnly = true)
    public boolean verifyChain() {

        List<AuditLog> logs =
                auditLogRepository.findAllByOrderByIdAsc();

        String expectedPreviousHash = GENESIS_HASH;

        for (AuditLog log : logs) {

            if (log.getPreviousHash() == null
                    || !expectedPreviousHash.equals(
                    log.getPreviousHash())) {
                return false;
            }

            String expectedCurrentHash = calculateHash(log);

            if (log.getCurrentHash() == null
                    || !expectedCurrentHash.equals(
                    log.getCurrentHash())) {
                return false;
            }

            expectedPreviousHash = log.getCurrentHash();
        }

        return true;
    }

    // Get logs for a specific entity
    public List<AuditLog> getLogsByEntity(
            String entity,
            Long entityId) {

        return auditLogRepository
                .findByEntityAndEntityId(entity, entityId);
    }

    // Get all audit logs in ID order
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByIdAsc();
    }

    // Calculate SHA-256 hash
    private String calculateHash(AuditLog log) {

        String data = String.join("|",
                safe(log.getPreviousHash()),
                safe(log.getUserId()),
                safe(log.getAction()),
                safe(log.getEntity()),
                safe(log.getEntityId()),
                safe(log.getOldValue()),
                safe(log.getNewValue()),
                safe(log.getTimestamp())
        );

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable",
                    e
            );
        }
    }

    private String safe(Object value) {
        return value == null ? "<null>" : value.toString();
    }
}
