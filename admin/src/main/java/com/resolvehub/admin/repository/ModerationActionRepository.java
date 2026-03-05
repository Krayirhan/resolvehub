package com.resolvehub.admin.repository;

import com.resolvehub.admin.domain.ModerationActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository extends JpaRepository<ModerationActionEntity, Long> {
}
