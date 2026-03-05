package com.resolvehub.playbook.service;

import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.playbook.domain.PlaybookEntity;
import com.resolvehub.playbook.dto.PlaybookResponse;
import com.resolvehub.playbook.repository.PlaybookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaybookService {
    private final PlaybookRepository playbookRepository;
    private final SolutionService solutionService;

    public PlaybookService(PlaybookRepository playbookRepository, SolutionService solutionService) {
        this.playbookRepository = playbookRepository;
        this.solutionService = solutionService;
    }

    @Transactional(readOnly = true)
    public PlaybookResponse getPlaybook(Long id) {
        PlaybookEntity playbook = playbookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playbook not found"));
        return solutionService.toPlaybookResponse(playbook);
    }

    @Transactional(readOnly = true)
    public List<PlaybookResponse> listPlaybooks() {
        return playbookRepository.findAll().stream()
                .map(solutionService::toPlaybookResponse)
                .toList();
    }
}
