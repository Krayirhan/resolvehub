package com.resolvehub.playbook.controller;

import com.resolvehub.playbook.dto.PlaybookResponse;
import com.resolvehub.playbook.dto.RecommendedResponse;
import com.resolvehub.playbook.service.PlaybookService;
import com.resolvehub.playbook.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PlaybookController {
    private final PlaybookService playbookService;
    private final RecommendationService recommendationService;

    public PlaybookController(PlaybookService playbookService, RecommendationService recommendationService) {
        this.playbookService = playbookService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/playbooks/{id}")
    public PlaybookResponse getPlaybook(@PathVariable Long id) {
        return playbookService.getPlaybook(id);
    }

    @GetMapping("/playbooks")
    public List<PlaybookResponse> listPlaybooks(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String query
    ) {
        return playbookService.listPlaybooks();
    }

    @GetMapping("/problems/{id}/recommended")
    public RecommendedResponse getRecommendations(@PathVariable("id") Long problemId) {
        return recommendationService.recommendForProblem(problemId);
    }
}
