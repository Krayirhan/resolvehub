package com.resolvehub.aiorchestrator.repository;

import com.resolvehub.aiorchestrator.domain.AiJobEntity;
import com.resolvehub.aiorchestrator.domain.AiJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiJobRepository extends JpaRepository<AiJobEntity, Long> {
    List<AiJobEntity> findTop20ByStatusOrderByCreatedAtAsc(AiJobStatus status);
}
