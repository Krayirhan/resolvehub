package com.resolvehub.problemgraph.repository;

import com.resolvehub.common.model.ProblemStatus;
import com.resolvehub.problemgraph.domain.ProblemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {
    @Query("""
            select p from ProblemEntity p
            where (:query is null or lower(p.title) like lower(concat('%', :query, '%'))
                or lower(p.description) like lower(concat('%', :query, '%')))
            and (:status is null or p.status = :status)
            """)
    Page<ProblemEntity> search(@Param("query") String query, @Param("status") ProblemStatus status, Pageable pageable);

    Page<ProblemEntity> findByIdIn(Collection<Long> ids, Pageable pageable);
}
