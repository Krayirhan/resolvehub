package com.resolvehub.problemgraph.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "problem_tags")
public class ProblemTagEntity {
    @EmbeddedId
    private ProblemTagId id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    public ProblemTagEntity() {
    }

    public ProblemTagEntity(ProblemTagId id) {
        this.id = id;
    }

    public ProblemTagId getId() {
        return id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }
}
