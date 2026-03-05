package com.resolvehub.search.controller;

import com.resolvehub.search.service.HybridSearchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final HybridSearchService hybridSearchService;

    public SearchController(HybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    @GetMapping
    public List<Map<String, Object>> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return hybridSearchService.search(query, Math.min(Math.max(limit, 1), 100));
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Map<String, String> reindex() {
        return Map.of("status", hybridSearchService.reindex());
    }
}
