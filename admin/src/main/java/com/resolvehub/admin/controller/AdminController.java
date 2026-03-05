package com.resolvehub.admin.controller;

import com.resolvehub.admin.dto.FlagResponse;
import com.resolvehub.admin.dto.ModerationActionRequest;
import com.resolvehub.admin.dto.ResolveFlagRequest;
import com.resolvehub.admin.service.AdminService;
import com.resolvehub.common.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/flags")
    public List<FlagResponse> listFlags() {
        return adminService.listFlags();
    }

    @PostMapping("/flags/{id}/resolve")
    public FlagResponse resolveFlag(@PathVariable Long id, @Valid @RequestBody ResolveFlagRequest request) {
        return adminService.resolveFlag(id, request, SecurityUtils.currentUserId());
    }

    @PostMapping("/actions")
    public Map<String, Object> applyAction(@Valid @RequestBody ModerationActionRequest request) {
        return adminService.applyAction(request, SecurityUtils.currentUserId());
    }
}
