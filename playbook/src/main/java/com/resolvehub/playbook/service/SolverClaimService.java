package com.resolvehub.playbook.service;

import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.playbook.domain.SolverClaimEntity;
import com.resolvehub.playbook.dto.CreateSolverClaimRequest;
import com.resolvehub.playbook.dto.SolverClaimResponse;
import com.resolvehub.playbook.dto.UpdateSolverClaimRequest;
import com.resolvehub.playbook.repository.SolverClaimRepository;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolverClaimService {
    private final SolverClaimRepository solverClaimRepository;
    private final ProblemRepository problemRepository;

    public SolverClaimService(SolverClaimRepository solverClaimRepository, ProblemRepository problemRepository) {
        this.solverClaimRepository = solverClaimRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public SolverClaimResponse createClaim(Long problemId, Long userId, CreateSolverClaimRequest request) {
        if (!problemRepository.existsById(problemId)) {
            throw new NotFoundException("Problem not found");
        }
        SolverClaimEntity claim = new SolverClaimEntity();
        claim.setProblemId(problemId);
        claim.setUserId(userId);
        claim.setMessage(request.message());
        solverClaimRepository.save(claim);
        return toResponse(claim);
    }

    @Transactional
    public SolverClaimResponse updateClaim(Long claimId, UpdateSolverClaimRequest request) {
        SolverClaimEntity claim = solverClaimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
        claim.setStatus(request.status());
        return toResponse(claim);
    }

    private SolverClaimResponse toResponse(SolverClaimEntity entity) {
        return new SolverClaimResponse(
                entity.getId(),
                entity.getProblemId(),
                entity.getUserId(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
