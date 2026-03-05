package com.resolvehub.search.service;

import com.resolvehub.playbook.repository.PlaybookRepository;
import com.resolvehub.playbook.repository.SolutionRepository;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class HybridSearchService {
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;
    private final PlaybookRepository playbookRepository;

    public HybridSearchService(
            ProblemRepository problemRepository,
            SolutionRepository solutionRepository,
            PlaybookRepository playbookRepository
    ) {
        this.problemRepository = problemRepository;
        this.solutionRepository = solutionRepository;
        this.playbookRepository = playbookRepository;
    }

    public List<Map<String, Object>> search(String query, int limit) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<Map<String, Object>> results = new ArrayList<>();

        problemRepository.findAll().stream()
                .filter(p -> p.getTitle().toLowerCase(Locale.ROOT).contains(q) || p.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .limit(limit)
                .forEach(problem -> results.add(Map.of(
                        "type", "problem",
                        "id", problem.getId(),
                        "title", problem.getTitle(),
                        "score", 0.7
                )));

        solutionRepository.findAll().stream()
                .filter(s -> s.getSummary().toLowerCase(Locale.ROOT).contains(q))
                .limit(Math.max(0, limit - results.size()))
                .forEach(solution -> results.add(Map.of(
                        "type", "solution",
                        "id", solution.getId(),
                        "title", solution.getSummary(),
                        "score", 0.65
                )));

        playbookRepository.findAll().stream()
                .filter(p -> p.getTitle().toLowerCase(Locale.ROOT).contains(q))
                .limit(Math.max(0, limit - results.size()))
                .forEach(playbook -> results.add(Map.of(
                        "type", "playbook",
                        "id", playbook.getId(),
                        "title", playbook.getTitle(),
                        "score", 0.6
                )));

        return results.stream().limit(limit).toList();
    }

    public String reindex() {
        // Placeholder for OpenSearch index writer job trigger.
        return "Reindex job accepted";
    }
}
