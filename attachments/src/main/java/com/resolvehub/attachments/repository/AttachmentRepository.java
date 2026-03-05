package com.resolvehub.attachments.repository;

import com.resolvehub.attachments.domain.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
}
