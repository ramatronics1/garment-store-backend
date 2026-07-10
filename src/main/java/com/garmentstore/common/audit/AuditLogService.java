package com.garmentstore.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorUserId,
                       String actorRole,
                       String actionType,
                       String entityType,
                       String entityId,
                       String afterJson,
                       String ipAddress) {
        auditLogRepository.save(AuditLog.builder()
                .actorUserId(actorUserId)
                .actorRole(actorRole)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .afterJson(afterJson)
                .ipAddress(ipAddress)
                .build());
    }
}
