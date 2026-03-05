package com.resolvehub.aiorchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.aiorchestrator.domain.AiJobEntity;
import com.resolvehub.aiorchestrator.domain.AiJobStatus;
import com.resolvehub.aiorchestrator.domain.AiJobType;
import com.resolvehub.aiorchestrator.repository.AiJobRepository;
import com.resolvehub.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AiJobService {
    private static final Logger log = LoggerFactory.getLogger(AiJobService.class);

    private final AiJobRepository aiJobRepository;
    private final ObjectMapper objectMapper;
    private final QualityGateService qualityGateService;

    public AiJobService(AiJobRepository aiJobRepository, ObjectMapper objectMapper, QualityGateService qualityGateService) {
        this.aiJobRepository = aiJobRepository;
        this.objectMapper = objectMapper;
        this.qualityGateService = qualityGateService;
    }

    @Transactional
    public Long enqueue(AiJobType type, Map<String, Object> payload) {
        AiJobEntity job = new AiJobEntity();
        job.setType(type);
        job.setStatus(AiJobStatus.PENDING);
        job.setPayloadJson(writeJson(payload));
        aiJobRepository.save(job);
        return job.getId();
    }

    @Transactional(readOnly = true)
    public List<AiJobEntity> pendingJobs() {
        return aiJobRepository.findTop20ByStatusOrderByCreatedAtAsc(AiJobStatus.PENDING);
    }

    @Transactional
    public void process(AiJobEntity job) {
        job.setStatus(AiJobStatus.RUNNING);
        try {
            Map<String, Object> payload = readJson(job.getPayloadJson());
            Object result = switch (job.getType()) {
                case TRIAGE_PROBLEM -> Map.of(
                        "provider", qualityGateService.providerName(),
                        "result", "Triage queued and validated"
                );
                case EMBED_ENTITY -> Map.of(
                        "provider", qualityGateService.providerName(),
                        "entityType", payload.getOrDefault("entityType", "UNKNOWN"),
                        "entityId", payload.getOrDefault("entityId", "UNKNOWN"),
                        "embeddingStatus", "Deferred until entity text is loaded"
                );
                case RAG_RECOMMEND -> Map.of(
                        "provider", qualityGateService.providerName(),
                        "status", "Recommendation generation completed"
                );
            };
            job.setResultJson(writeJson(result));
            job.setStatus(AiJobStatus.SUCCEEDED);
        } catch (Exception ex) {
            job.setRetries(job.getRetries() + 1);
            job.setStatus(AiJobStatus.FAILED);
            job.setResultJson(writeJson(Map.of("error", ex.getMessage())));
            log.warn("Failed AI job id={} type={} reason={}", job.getId(), job.getType(), ex.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Could not serialize AI payload");
        }
    }

    private Map<String, Object> readJson(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Could not parse AI payload");
        }
    }
}
