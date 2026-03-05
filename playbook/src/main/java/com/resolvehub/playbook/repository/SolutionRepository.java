package com.resolvehub.playbook.repository;

import com.resolvehub.playbook.domain.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolutionRepository extends JpaRepository<SolutionEntity, Long> {
    List<SolutionEntity> findByProblemIdOrderByCreatedAtDesc(Long problemId);

    List<SolutionEntity> findByAuthorId(Long authorId);
}
