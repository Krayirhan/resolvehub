package com.resolvehub.playbook.domain;

import com.resolvehub.common.model.OutcomeType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "solution_outcomes")
public class SolutionOutcomeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solution_id", nullable = false)
    private Long solutionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "environment_fingerprint", nullable = false, length = 128)
    private String environmentFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutcomeType outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public SolutionOutcomeEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEnvironmentFingerprint() {
        return environmentFingerprint;
    }

    public void setEnvironmentFingerprint(String environmentFingerprint) {
        this.environmentFingerprint = environmentFingerprint;
    }

    public OutcomeType getOutcome() {
        return outcome;
    }

    public void setOutcome(OutcomeType outcome) {
        this.outcome = outcome;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
