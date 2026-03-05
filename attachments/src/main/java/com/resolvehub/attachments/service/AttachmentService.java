package com.resolvehub.attachments.service;

import com.resolvehub.attachments.domain.AttachmentEntity;
import com.resolvehub.attachments.dto.AttachmentResponse;
import com.resolvehub.attachments.repository.AttachmentRepository;
import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final ProblemRepository problemRepository;
    private final MinioStorageService minioStorageService;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            ProblemRepository problemRepository,
            MinioStorageService minioStorageService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.problemRepository = problemRepository;
        this.minioStorageService = minioStorageService;
    }

    @Transactional
    public AttachmentResponse uploadProblemAttachment(Long problemId, MultipartFile file) {
        if (!problemRepository.existsById(problemId)) {
            throw new NotFoundException("Problem not found");
        }
        String safeName = file.getOriginalFilename() == null ? "file.bin" : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = "problems/" + problemId + "/" + UUID.randomUUID() + "-" + safeName;

        minioStorageService.upload(objectKey, file);

        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setOwnerType("PROBLEM");
        attachment.setOwnerId(problemId);
        attachment.setObjectKey(objectKey);
        attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        attachment.setSize(file.getSize());
        attachmentRepository.save(attachment);
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOwnerType(),
                attachment.getOwnerId(),
                attachment.getObjectKey(),
                attachment.getContentType(),
                attachment.getSize(),
                attachment.getCreatedAt()
        );
    }
}
