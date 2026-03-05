package com.resolvehub.playbook.repository;

import com.resolvehub.playbook.domain.SolutionOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SolutionOutcomeRepository extends JpaRepository<SolutionOutcomeEntity, Long> {
    List<SolutionOutcomeEntity> findBySolutionId(Long solutionId);

    List<SolutionOutcomeEntity> findBySolutionIdIn(Collection<Long> solutionIds);
}
