package com.resolvehub.playbook.repository;

import com.resolvehub.playbook.domain.SolutionVoteEntity;
import com.resolvehub.playbook.domain.SolutionVoteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolutionVoteRepository extends JpaRepository<SolutionVoteEntity, SolutionVoteId> {
    List<SolutionVoteEntity> findByIdSolutionId(Long solutionId);
}
