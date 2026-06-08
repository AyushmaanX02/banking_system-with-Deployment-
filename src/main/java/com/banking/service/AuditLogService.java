package com.banking.service;

import com.banking.entity.AuditLog;
import com.banking.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String email, String action, String entity, String entityId, String details) {
        AuditLog log = AuditLog.builder()
                .userEmail(email != null ? email : "SYSTEM")
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .ipAddress("127.0.0.1") // Default internal/loopback IP
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
