package com.resolvehub.playbook.repository;

import com.resolvehub.playbook.domain.PlaybookStepEntity;
import com.resolvehub.playbook.domain.PlaybookStepId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaybookStepRepository extends JpaRepository<PlaybookStepEntity, PlaybookStepId> {
    List<PlaybookStepEntity> findByIdPlaybookIdOrderByIdStepNoAsc(Long playbookId);
}
