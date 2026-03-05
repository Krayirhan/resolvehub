package com.resolvehub.playbook.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "playbooks")
public class PlaybookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_solution_id", nullable = false)
    private Long sourceSolutionId;

    @Column(nullable = false)
    private int version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaybookStatus status = PlaybookStatus.DRAFT;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

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

    public PlaybookEntity() {
    }

    public Long getId() {
        return id;
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

    public Long getSourceSolutionId() {
        return sourceSolutionId;
    }

    public void setSourceSolutionId(Long sourceSolutionId) {
        this.sourceSolutionId = sourceSolutionId;
    }

    public int getVersion() {
        return version;
    }

    public PlaybookStatus getStatus() {
        return status;
    }

    public void setStatus(PlaybookStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
