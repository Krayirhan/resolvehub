package com.resolvehub.problemgraph.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.aiorchestrator.domain.AiJobType;
import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;
import com.resolvehub.aiorchestrator.service.AiJobService;
import com.resolvehub.aiorchestrator.service.QualityGateService;
import com.resolvehub.common.api.PageResponse;
import com.resolvehub.common.exception.BadRequestException;
import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.common.model.ProblemStatus;
import com.resolvehub.problemgraph.domain.ProblemEntity;
import com.resolvehub.problemgraph.domain.ProblemTagEntity;
import com.resolvehub.problemgraph.domain.ProblemTagId;
import com.resolvehub.problemgraph.dto.CreateProblemRequest;
import com.resolvehub.problemgraph.dto.ProblemResponse;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import com.resolvehub.problemgraph.repository.ProblemTagRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemTagRepository problemTagRepository;
    private final QualityGateService qualityGateService;
    private final AiJobService aiJobService;
    private final ObjectMapper objectMapper;

    public ProblemService(
            ProblemRepository problemRepository,
            ProblemTagRepository problemTagRepository,
            QualityGateService qualityGateService,
            AiJobService aiJobService,
            ObjectMapper objectMapper
    ) {
        this.problemRepository = problemRepository;
        this.problemTagRepository = problemTagRepository;
        this.qualityGateService = qualityGateService;
        this.aiJobService = aiJobService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request, Long authorId) {
        ProblemTriageResult triageResult = qualityGateService.triageProblem(request.title(), request.description());
        ProblemEntity problem = new ProblemEntity();
        problem.setAuthorId(authorId);
        problem.setTitle(request.title().trim());
        problem.setDescription(triageResult.redactedDescription());
        problem.setCategory(request.category().trim().toLowerCase());
        problem.setEnvironmentJson(writeJson(request.environment()));
        problem.setStatus(ProblemStatus.OPEN);
        problemRepository.save(problem);

        Set<String> normalizedTags = new HashSet<>(triageResult.suggestedTags());
        normalizedTags.addAll(qualityGateService.inferTagsFromEnvironment(request.environment()));
        normalizedTags.stream()
                .map(tag -> tag.trim().toLowerCase())
                .filter(tag -> !tag.isBlank())
                .forEach(tag -> problemTagRepository.save(new ProblemTagEntity(new ProblemTagId(problem.getId(), tag))));

        aiJobService.enqueue(AiJobType.TRIAGE_PROBLEM, Map.of("problemId", problem.getId(), "authorId", authorId));
        aiJobService.enqueue(AiJobType.EMBED_ENTITY, Map.of("entityType", "PROBLEM", "entityId", problem.getId()));

        return toResponse(problem, "PENDING");
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblem(Long id) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Problem not found"));
        return toResponse(problem, "N/A");
    }

    @Transactional(readOnly = true)
    public PageResponse<ProblemResponse> listProblems(String query, String tag, ProblemStatus status, int page, int size, String sort) {
        Sort sortSpec = switch (sort == null ? "recent" : sort.toLowerCase()) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "updated" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sortSpec);

        Page<ProblemEntity> results;
        if (tag != null && !tag.isBlank()) {
            List<Long> ids = problemTagRepository.findProblemIdsByTag(tag.trim().toLowerCase());
            if (ids.isEmpty()) {
                return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
            }
            List<ProblemEntity> filtered = problemRepository.findAllById(ids).stream()
                    .filter(p -> matchesSearch(p, query, status))
                    .sorted(sortSpec.getOrderFor("createdAt") != null && sortSpec.getOrderFor("createdAt").isAscending()
                            ? Comparator.comparing(ProblemEntity::getCreatedAt)
                            : Comparator.comparing(ProblemEntity::getCreatedAt).reversed())
                    .toList();
            int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), filtered.size());
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<ProblemEntity> pagedItems = filtered.subList(start, end);
            results = new PageImpl<>(pagedItems, pageable, filtered.size());
        } else {
            results = problemRepository.search(blankToNull(query), status, pageable);
        }
        return PageResponse.from(results.map(problem -> toResponse(problem, "N/A")));
    }

    @Transactional
    public ProblemResponse setCanonicalProblem(Long problemId, Long canonicalProblemId) {
        if (Objects.equals(problemId, canonicalProblemId)) {
            throw new BadRequestException("Problem cannot canonicalize itself");
        }
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("Problem not found"));
        if (!problemRepository.existsById(canonicalProblemId)) {
            throw new NotFoundException("Canonical problem not found");
        }
        problem.setCanonicalProblemId(canonicalProblemId);
        return toResponse(problem, "N/A");
    }

    @Transactional
    public ProblemResponse addTag(Long problemId, String tag) {
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("Problem not found"));
        String normalized = tag.trim().toLowerCase();
        if (!normalized.isBlank()) {
            problemTagRepository.save(new ProblemTagEntity(new ProblemTagId(problemId, normalized)));
        }
        return toResponse(problem, "N/A");
    }

    private boolean matchesSearch(ProblemEntity problem, String query, ProblemStatus status) {
        boolean statusMatches = status == null || problem.getStatus() == status;
        if (query == null || query.isBlank()) {
            return statusMatches;
        }
        String q = query.toLowerCase();
        return statusMatches && (problem.getTitle().toLowerCase().contains(q) || problem.getDescription().toLowerCase().contains(q));
    }

    private ProblemResponse toResponse(ProblemEntity problem, String triageStatus) {
        List<String> tags = problemTagRepository.findByIdProblemId(problem.getId()).stream()
                .map(t -> t.getId().getTag())
                .sorted()
                .toList();
        return new ProblemResponse(
                problem.getId(),
                problem.getAuthorId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getCategory(),
                readJson(problem.getEnvironmentJson()),
                problem.getStatus(),
                problem.getCanonicalProblemId(),
                tags,
                triageStatus,
                problem.getCreatedAt(),
                problem.getUpdatedAt()
        );
    }

    private String writeJson(Map<String, String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid environment payload");
        }
    }

    private Map<String, String> readJson(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
