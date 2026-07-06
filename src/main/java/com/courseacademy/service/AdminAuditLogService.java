package com.courseacademy.service;

import com.courseacademy.entity.AdminAuditLog;
import com.courseacademy.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    public void log(String adminName, String action, String targetType, String targetId, String details) {
        AdminAuditLog log = AdminAuditLog.builder()
                .adminName(adminName)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        adminAuditLogRepository.save(log);
    }
}

