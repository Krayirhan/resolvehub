package com.resolvehub.problemgraph.domain;

import com.resolvehub.common.model.ProblemStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "problems")
public class ProblemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "environment_json", nullable = false, columnDefinition = "TEXT")
    private String environmentJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProblemStatus status = ProblemStatus.OPEN;

    @Column(name = "canonical_problem_id")
    private Long canonicalProblemId;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public ProblemEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEnvironmentJson() {
        return environmentJson;
    }

    public void setEnvironmentJson(String environmentJson) {
        this.environmentJson = environmentJson;
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemStatus status) {
        this.status = status;
    }

    public Long getCanonicalProblemId() {
        return canonicalProblemId;
    }

    public void setCanonicalProblemId(Long canonicalProblemId) {
        this.canonicalProblemId = canonicalProblemId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
