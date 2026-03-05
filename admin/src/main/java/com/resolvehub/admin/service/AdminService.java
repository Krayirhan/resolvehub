package com.resolvehub.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.admin.domain.AuditLogEntity;
import com.resolvehub.admin.domain.FlagEntity;
import com.resolvehub.admin.domain.ModerationActionEntity;
import com.resolvehub.admin.dto.FlagResponse;
import com.resolvehub.admin.dto.ModerationActionRequest;
import com.resolvehub.admin.dto.ResolveFlagRequest;
import com.resolvehub.admin.repository.AuditLogRepository;
import com.resolvehub.admin.repository.FlagRepository;
import com.resolvehub.admin.repository.ModerationActionRepository;
import com.resolvehub.common.exception.BadRequestException;
import com.resolvehub.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final FlagRepository flagRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminService(
            FlagRepository flagRepository,
            ModerationActionRepository moderationActionRepository,
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.flagRepository = flagRepository;
        this.moderationActionRepository = moderationActionRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<FlagResponse> listFlags() {
        return flagRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public FlagResponse resolveFlag(Long flagId, ResolveFlagRequest request, Long moderatorId) {
        FlagEntity flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new NotFoundException("Flag not found"));
        flag.setStatus(request.status().toUpperCase());
        audit(moderatorId, "FLAG_RESOLVED", "FLAG", flagId, Map.of("status", flag.getStatus()));
        return toResponse(flag);
    }

    @Transactional
    public Map<String, Object> applyAction(ModerationActionRequest request, Long moderatorId) {
        ModerationActionEntity action = new ModerationActionEntity();
        action.setModeratorId(moderatorId);
        action.setActionType(request.actionType());
        action.setEntityType(request.entityType());
        action.setEntityId(request.entityId());
        action.setDetailsJson(toJson(request.details()));
        moderationActionRepository.save(action);

        audit(moderatorId, "MODERATION_ACTION", request.entityType(), request.entityId(), request.details());
        return Map.of(
                "actionId", action.getId(),
                "status", "APPLIED"
        );
    }

    public void audit(Long actorId, String eventType, String entityType, Long entityId, Map<String, Object> details) {
        AuditLogEntity log = new AuditLogEntity();
        log.setActorId(actorId);
        log.setEventType(eventType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetailsJson(toJson(details));
        auditLogRepository.save(log);
    }

    private FlagResponse toResponse(FlagEntity flag) {
        return new FlagResponse(
                flag.getId(),
                flag.getEntityType(),
                flag.getEntityId(),
                flag.getReason(),
                flag.getCreatedBy(),
                flag.getStatus(),
                flag.getCreatedAt()
        );
    }

    private String toJson(Object details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Unable to serialize moderation details");
        }
    }
}
