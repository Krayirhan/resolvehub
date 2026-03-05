package com.resolvehub.problemgraph.repository;

import com.resolvehub.problemgraph.domain.ProblemTagEntity;
import com.resolvehub.problemgraph.domain.ProblemTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemTagRepository extends JpaRepository<ProblemTagEntity, ProblemTagId> {
    List<ProblemTagEntity> findByIdProblemId(Long problemId);

    @Query("select t.id.problemId from ProblemTagEntity t where t.id.tag = :tag")
    List<Long> findProblemIdsByTag(String tag);
}
