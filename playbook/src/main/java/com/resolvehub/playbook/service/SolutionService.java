package com.resolvehub.playbook.service;

import com.resolvehub.aiorchestrator.domain.AiJobType;
import com.resolvehub.aiorchestrator.service.AiJobService;
import com.resolvehub.aiorchestrator.service.QualityGateService;
import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.common.util.EnvironmentFingerprintUtil;
import com.resolvehub.playbook.domain.*;
import com.resolvehub.playbook.dto.*;
import com.resolvehub.playbook.repository.*;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SolutionService {
    private final SolutionRepository solutionRepository;
    private final SolutionVoteRepository solutionVoteRepository;
    private final SolutionOutcomeRepository solutionOutcomeRepository;
    private final PlaybookRepository playbookRepository;
    private final PlaybookStepRepository playbookStepRepository;
    private final ProblemRepository problemRepository;
    private final QualityGateService qualityGateService;
    private final AiJobService aiJobService;

    public SolutionService(
            SolutionRepository solutionRepository,
            SolutionVoteRepository solutionVoteRepository,
            SolutionOutcomeRepository solutionOutcomeRepository,
            PlaybookRepository playbookRepository,
            PlaybookStepRepository playbookStepRepository,
            ProblemRepository problemRepository,
            QualityGateService qualityGateService,
            AiJobService aiJobService
    ) {
        this.solutionRepository = solutionRepository;
        this.solutionVoteRepository = solutionVoteRepository;
        this.solutionOutcomeRepository = solutionOutcomeRepository;
        this.playbookRepository = playbookRepository;
        this.playbookStepRepository = playbookStepRepository;
        this.problemRepository = problemRepository;
        this.qualityGateService = qualityGateService;
        this.aiJobService = aiJobService;
    }

    @Transactional
    public SolutionResponse createSolution(Long problemId, Long userId, CreateSolutionRequest request) {
        if (!problemRepository.existsById(problemId)) {
            throw new NotFoundException("Problem not found");
        }
        qualityGateService.validateSolutionStructure(
                request.stepsMarkdown(),
                request.risksMarkdown(),
                request.verificationMarkdown(),
                request.rollbackMarkdown()
        );

        SolutionEntity solution = new SolutionEntity();
        solution.setProblemId(problemId);
        solution.setAuthorId(userId);
        solution.setRootCauseId(request.rootCauseId());
        solution.setFixId(request.fixId());
        solution.setSummary(request.summary());
        solution.setStepsMarkdown(request.stepsMarkdown());
        solution.setRisksMarkdown(request.risksMarkdown());
        solution.setRollbackMarkdown(request.rollbackMarkdown());
        solution.setVerificationMarkdown(request.verificationMarkdown());
        solutionRepository.save(solution);

        aiJobService.enqueue(AiJobType.EMBED_ENTITY, Map.of(
                "entityType", "SOLUTION",
                "entityId", solution.getId()
        ));
        return toResponse(solution);
    }

    @Transactional(readOnly = true)
    public List<SolutionResponse> listSolutions(Long problemId) {
        return solutionRepository.findByProblemIdOrderByCreatedAtDesc(problemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolutionResponse vote(Long solutionId, Long userId, VoteSolutionRequest request) {
        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));
        SolutionVoteId voteId = new SolutionVoteId(solutionId, userId);
        SolutionVoteEntity voteEntity = solutionVoteRepository.findById(voteId).orElseGet(() -> new SolutionVoteEntity(voteId, request.vote()));
        voteEntity.setVote(request.vote());
        solutionVoteRepository.save(voteEntity);
        return toResponse(solution);
    }

    @Transactional
    public OutcomeResponse recordOutcome(Long solutionId, Long userId, RecordOutcomeRequest request) {
        solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));

        String fingerprint = request.environmentFingerprint();
        if (fingerprint == null || fingerprint.isBlank()) {
            Map<String, String> env = request.environment() == null ? Map.of() : request.environment();
            fingerprint = env.isEmpty() ? "unknown" : EnvironmentFingerprintUtil.fingerprint(env);
        }

        SolutionOutcomeEntity outcome = new SolutionOutcomeEntity();
        outcome.setSolutionId(solutionId);
        outcome.setUserId(userId);
        outcome.setEnvironmentFingerprint(fingerprint);
        outcome.setOutcome(request.outcome());
        outcome.setNotes(request.notes());
        solutionOutcomeRepository.save(outcome);
        return new OutcomeResponse(
                outcome.getId(),
                outcome.getSolutionId(),
                outcome.getUserId(),
                outcome.getEnvironmentFingerprint(),
                outcome.getOutcome(),
                outcome.getNotes(),
                outcome.getCreatedAt()
        );
    }

    @Transactional
    public PlaybookResponse promoteToPlaybook(Long solutionId, PromotePlaybookRequest request) {
        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));
        PlaybookEntity existing = playbookRepository.findBySourceSolutionId(solutionId).orElse(null);
        if (existing != null) {
            return toPlaybookResponse(existing);
        }

        PlaybookEntity playbook = new PlaybookEntity();
        playbook.setTitle(request.title());
        playbook.setDescription(request.description());
        playbook.setSourceSolutionId(solutionId);
        playbook.setStatus(PlaybookStatus.PUBLISHED);
        playbookRepository.save(playbook);

        PlaybookStepEntity step = new PlaybookStepEntity(
                new PlaybookStepId(playbook.getId(), 1),
                "Execution Steps",
                solution.getStepsMarkdown()
        );
        playbookStepRepository.save(step);
        return toPlaybookResponse(playbook);
    }

    private SolutionResponse toResponse(SolutionEntity solution) {
        int voteScore = solutionVoteRepository.findByIdSolutionId(solution.getId())
                .stream()
                .mapToInt(SolutionVoteEntity::getVote)
                .sum();
        return new SolutionResponse(
                solution.getId(),
                solution.getProblemId(),
                solution.getAuthorId(),
                solution.getRootCauseId(),
                solution.getFixId(),
                solution.getSummary(),
                solution.getStepsMarkdown(),
                solution.getRisksMarkdown(),
                solution.getRollbackMarkdown(),
                solution.getVerificationMarkdown(),
                voteScore,
                solution.getCreatedAt(),
                solution.getUpdatedAt()
        );
    }

    PlaybookResponse toPlaybookResponse(PlaybookEntity playbook) {
        List<PlaybookResponse.PlaybookStepResponse> steps = playbookStepRepository.findByIdPlaybookIdOrderByIdStepNoAsc(playbook.getId()).stream()
                .map(step -> new PlaybookResponse.PlaybookStepResponse(
                        step.getId().getStepNo(),
                        step.getTitle(),
                        step.getContentMarkdown()
                ))
                .toList();
        return new PlaybookResponse(
                playbook.getId(),
                playbook.getTitle(),
                playbook.getDescription(),
                playbook.getSourceSolutionId(),
                playbook.getVersion(),
                playbook.getStatus(),
                steps,
                playbook.getCreatedAt()
        );
    }
}
