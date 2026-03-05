package com.resolvehub.aiorchestrator.service;

import com.resolvehub.aiorchestrator.repository.AiJobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AiJobWorker {
    private final AiJobService aiJobService;
    private final AiJobRepository aiJobRepository;

    public AiJobWorker(AiJobService aiJobService, AiJobRepository aiJobRepository) {
        this.aiJobService = aiJobService;
        this.aiJobRepository = aiJobRepository;
    }

    @Scheduled(fixedDelayString = "${resolvehub.ai.worker-delay-ms:15000}")
    @Transactional
    public void processQueue() {
        aiJobService.pendingJobs().forEach(job -> {
            aiJobService.process(job);
            aiJobRepository.save(job);
        });
    }
}
