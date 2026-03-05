package com.resolvehub.problemgraph.controller;

import com.resolvehub.common.api.PageResponse;
import com.resolvehub.common.model.ProblemStatus;
import com.resolvehub.common.security.SecurityUtils;
import com.resolvehub.problemgraph.dto.AddTagRequest;
import com.resolvehub.problemgraph.dto.CanonicalProblemRequest;
import com.resolvehub.problemgraph.dto.CreateProblemRequest;
import com.resolvehub.problemgraph.dto.ProblemResponse;
import com.resolvehub.problemgraph.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/problems")
public class ProblemController {
    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> create(@Valid @RequestBody CreateProblemRequest request) {
        ProblemResponse response = problemService.createProblem(request, SecurityUtils.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ProblemResponse get(@PathVariable Long id) {
        return problemService.getProblem(id);
    }

    @GetMapping
    public PageResponse<ProblemResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) ProblemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return problemService.listProblems(query, tag, status, page, size, sort);
    }

    @PostMapping("/{id}/canonical")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ProblemResponse setCanonical(@PathVariable Long id, @Valid @RequestBody CanonicalProblemRequest request) {
        return problemService.setCanonicalProblem(id, request.canonicalProblemId());
    }

    @PostMapping("/{id}/tags")
    public ProblemResponse addTag(@PathVariable Long id, @Valid @RequestBody AddTagRequest request) {
        return problemService.addTag(id, request.tag());
    }
}
