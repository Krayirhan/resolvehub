package com.resolvehub.playbook.repository;

import com.resolvehub.playbook.domain.PlaybookEntity;
import com.resolvehub.playbook.domain.PlaybookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaybookRepository extends JpaRepository<PlaybookEntity, Long> {
    List<PlaybookEntity> findByStatus(PlaybookStatus status);

    Optional<PlaybookEntity> findBySourceSolutionId(Long sourceSolutionId);
}
