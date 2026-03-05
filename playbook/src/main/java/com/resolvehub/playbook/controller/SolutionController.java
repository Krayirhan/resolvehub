package com.resolvehub.playbook.controller;

import com.resolvehub.common.security.SecurityUtils;
import com.resolvehub.playbook.dto.*;
import com.resolvehub.playbook.service.SolutionService;
import com.resolvehub.playbook.service.SolverClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SolutionController {
    private final SolverClaimService solverClaimService;
    private final SolutionService solutionService;

    public SolutionController(SolverClaimService solverClaimService, SolutionService solutionService) {
        this.solverClaimService = solverClaimService;
        this.solutionService = solutionService;
    }

    @PostMapping("/problems/{id}/claims")
    public ResponseEntity<SolverClaimResponse> createClaim(@PathVariable("id") Long problemId, @Valid @RequestBody CreateSolverClaimRequest request) {
        SolverClaimResponse response = solverClaimService.createClaim(problemId, SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/claims/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public SolverClaimResponse updateClaim(@PathVariable("id") Long claimId, @Valid @RequestBody UpdateSolverClaimRequest request) {
        return solverClaimService.updateClaim(claimId, request);
    }

    @PostMapping("/problems/{id}/solutions")
    public ResponseEntity<SolutionResponse> createSolution(@PathVariable("id") Long problemId, @Valid @RequestBody CreateSolutionRequest request) {
        SolutionResponse response = solutionService.createSolution(problemId, SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/problems/{id}/solutions")
    public List<SolutionResponse> listSolutions(@PathVariable("id") Long problemId) {
        return solutionService.listSolutions(problemId);
    }

    @PostMapping("/solutions/{id}/vote")
    public SolutionResponse vote(@PathVariable("id") Long solutionId, @Valid @RequestBody VoteSolutionRequest request) {
        return solutionService.vote(solutionId, SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/solutions/{id}/outcomes")
    public ResponseEntity<OutcomeResponse> recordOutcome(@PathVariable("id") Long solutionId, @Valid @RequestBody RecordOutcomeRequest request) {
        OutcomeResponse response = solutionService.recordOutcome(solutionId, SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/solutions/{id}/promote-to-playbook")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public PlaybookResponse promoteToPlaybook(@PathVariable("id") Long solutionId, @Valid @RequestBody PromotePlaybookRequest request) {
        return solutionService.promoteToPlaybook(solutionId, request);
    }
}
