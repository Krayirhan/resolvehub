package com.resolvehub.playbook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.aiorchestrator.service.QualityGateService;
import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.common.model.OutcomeType;
import com.resolvehub.common.util.EnvironmentFingerprintUtil;
import com.resolvehub.playbook.domain.PlaybookEntity;
import com.resolvehub.playbook.domain.PlaybookStatus;
import com.resolvehub.playbook.domain.SolutionEntity;
import com.resolvehub.playbook.domain.SolutionOutcomeEntity;
import com.resolvehub.playbook.dto.RecommendedResponse;
import com.resolvehub.playbook.repository.PlaybookRepository;
import com.resolvehub.playbook.repository.SolutionOutcomeRepository;
import com.resolvehub.playbook.repository.SolutionRepository;
import com.resolvehub.problemgraph.domain.ProblemEntity;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;
    private final SolutionOutcomeRepository solutionOutcomeRepository;
    private final PlaybookRepository playbookRepository;
    private final QualityGateService qualityGateService;
    private final ObjectMapper objectMapper;
    private final RankingFormula rankingFormula;

    public RecommendationService(
            ProblemRepository problemRepository,
            SolutionRepository solutionRepository,
            SolutionOutcomeRepository solutionOutcomeRepository,
            PlaybookRepository playbookRepository,
            QualityGateService qualityGateService,
            ObjectMapper objectMapper,
            RankingFormula rankingFormula
    ) {
        this.problemRepository = problemRepository;
        this.solutionRepository = solutionRepository;
        this.solutionOutcomeRepository = solutionOutcomeRepository;
        this.playbookRepository = playbookRepository;
        this.qualityGateService = qualityGateService;
        this.objectMapper = objectMapper;
        this.rankingFormula = rankingFormula;
    }

    @Transactional(readOnly = true)
    public RecommendedResponse recommendForProblem(Long problemId) {
        ProblemEntity currentProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("Problem not found"));
        String envFingerprint = fingerprint(currentProblem.getEnvironmentJson());

        String problemText = currentProblem.getTitle() + "\n" + currentProblem.getDescription();
        List<Double> problemEmbedding = qualityGateService.embeddingFor(problemText);

        List<RecommendedResponse.RecommendedSolutionCard> topSolutions = solutionRepository.findByProblemIdOrderByCreatedAtDesc(problemId).stream()
                .map(solution -> scoreSolution(solution, problemText, problemEmbedding, envFingerprint))
                .sorted(Comparator.comparingDouble(c -> -c.ranking().totalScore()))
                .limit(10)
                .toList();

        Map<Long, RecommendedResponse.RankingBreakdown> scoreBySolution = topSolutions.stream()
                .collect(Collectors.toMap(
                        RecommendedResponse.RecommendedSolutionCard::solutionId,
                        RecommendedResponse.RecommendedSolutionCard::ranking
                ));

        List<RecommendedResponse.RecommendedPlaybookCard> playbooks = playbookRepository.findByStatus(PlaybookStatus.PUBLISHED).stream()
                .map(playbook -> mapPlaybook(playbook, scoreBySolution))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(c -> -c.ranking().totalScore()))
                .limit(10)
                .toList();

        List<RecommendedResponse.SimilarProblemCard> similarProblems = problemRepository.findAll().stream()
                .filter(problem -> !Objects.equals(problem.getId(), problemId))
                .map(problem -> scoreSimilarProblem(problem, problemText))
                .sorted(Comparator.comparingDouble(RecommendedResponse.SimilarProblemCard::score).reversed())
                .limit(5)
                .toList();

        return new RecommendedResponse(similarProblems, playbooks, topSolutions);
    }

    private RecommendedResponse.RecommendedSolutionCard scoreSolution(
            SolutionEntity solution,
            String problemText,
            List<Double> problemEmbedding,
            String environmentFingerprint
    ) {
        String solutionText = solution.getSummary() + "\n" + solution.getStepsMarkdown();
        List<Double> solutionEmbedding = qualityGateService.embeddingFor(solutionText);

        double vectorSimilarity = cosine(problemEmbedding, solutionEmbedding);
        double keywordScore = keywordSimilarity(problemText, solutionText);
        double successRateContext = successRateForSolution(solution.getId(), environmentFingerprint);
        double recencyDecay = recencyScore(solution.getCreatedAt());
        double authorExpertise = authorExpertise(solution.getAuthorId());

        double total = rankingFormula.score(
                vectorSimilarity,
                keywordScore,
                successRateContext,
                recencyDecay,
                authorExpertise
        );

        RecommendedResponse.RankingBreakdown breakdown = new RecommendedResponse.RankingBreakdown(
                round(total),
                round(vectorSimilarity),
                round(keywordScore),
                round(successRateContext),
                round(recencyDecay),
                round(authorExpertise),
                "w1*vector + w2*keyword + w3*context success + w4*recency + w5*author expertise"
        );
        return new RecommendedResponse.RecommendedSolutionCard(solution.getId(), solution.getSummary(), breakdown);
    }

    private RecommendedResponse.RecommendedPlaybookCard mapPlaybook(
            PlaybookEntity playbook,
            Map<Long, RecommendedResponse.RankingBreakdown> scoreBySolution
    ) {
        RecommendedResponse.RankingBreakdown sourceScore = scoreBySolution.get(playbook.getSourceSolutionId());
        if (sourceScore == null) {
            return null;
        }
        return new RecommendedResponse.RecommendedPlaybookCard(
                playbook.getId(),
                playbook.getTitle(),
                playbook.getSourceSolutionId(),
                sourceScore
        );
    }

    private RecommendedResponse.SimilarProblemCard scoreSimilarProblem(ProblemEntity candidate, String currentProblemText) {
        double score = keywordSimilarity(currentProblemText, candidate.getTitle() + "\n" + candidate.getDescription());
        return new RecommendedResponse.SimilarProblemCard(
                candidate.getId(),
                candidate.getTitle(),
                round(score),
                "keyword overlap against current problem"
        );
    }

    private double successRateForSolution(Long solutionId, String envFingerprint) {
        List<SolutionOutcomeEntity> outcomes = solutionOutcomeRepository.findBySolutionId(solutionId);
        if (outcomes.isEmpty()) {
            return 0.0;
        }
        List<SolutionOutcomeEntity> context = outcomes.stream()
                .filter(outcome -> Objects.equals(outcome.getEnvironmentFingerprint(), envFingerprint))
                .toList();
        List<SolutionOutcomeEntity> base = context.isEmpty() ? outcomes : context;
        double weighted = base.stream().mapToDouble(this::scoreOutcome).sum();
        return weighted / base.size();
    }

    private double authorExpertise(Long authorId) {
        List<Long> authorSolutionIds = solutionRepository.findByAuthorId(authorId).stream()
                .map(SolutionEntity::getId)
                .toList();
        if (authorSolutionIds.isEmpty()) {
            return 0.0;
        }
        List<SolutionOutcomeEntity> outcomes = solutionOutcomeRepository.findBySolutionIdIn(authorSolutionIds);
        if (outcomes.isEmpty()) {
            return 0.0;
        }
        return outcomes.stream().mapToDouble(this::scoreOutcome).average().orElse(0.0);
    }

    private double scoreOutcome(SolutionOutcomeEntity outcome) {
        return switch (outcome.getOutcome()) {
            case WORKED -> 1.0;
            case PARTIAL -> 0.5;
            case FAILED -> 0.0;
        };
    }

    private double recencyScore(Instant createdAt) {
        long days = Math.max(0, Duration.between(createdAt, Instant.now()).toDays());
        return Math.exp(-days / 90.0);
    }

    private double cosine(List<Double> a, List<Double> b) {
        int limit = Math.min(a.size(), b.size());
        if (limit == 0) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < limit; i++) {
            double va = a.get(i);
            double vb = b.get(i);
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double keywordSimilarity(String textA, String textB) {
        Set<String> a = tokens(textA);
        Set<String> b = tokens(textB);
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokens(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    private String fingerprint(String environmentJson) {
        try {
            Map<String, String> environment = objectMapper.readValue(environmentJson, new TypeReference<>() {
            });
            return EnvironmentFingerprintUtil.fingerprint(environment);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
