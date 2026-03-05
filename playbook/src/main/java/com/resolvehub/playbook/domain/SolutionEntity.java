package com.resolvehub.playbook.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "solutions")
public class SolutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "root_cause_id")
    private Long rootCauseId;

    @Column(name = "fix_id")
    private Long fixId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "steps_markdown", nullable = false, columnDefinition = "TEXT")
    private String stepsMarkdown;

    @Column(name = "risks_markdown", nullable = false, columnDefinition = "TEXT")
    private String risksMarkdown;

    @Column(name = "rollback_markdown", nullable = false, columnDefinition = "TEXT")
    private String rollbackMarkdown;

    @Column(name = "verification_markdown", nullable = false, columnDefinition = "TEXT")
    private String verificationMarkdown;

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

    public SolutionEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getRootCauseId() {
        return rootCauseId;
    }

    public void setRootCauseId(Long rootCauseId) {
        this.rootCauseId = rootCauseId;
    }

    public Long getFixId() {
        return fixId;
    }

    public void setFixId(Long fixId) {
        this.fixId = fixId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStepsMarkdown() {
        return stepsMarkdown;
    }

    public void setStepsMarkdown(String stepsMarkdown) {
        this.stepsMarkdown = stepsMarkdown;
    }

    public String getRisksMarkdown() {
        return risksMarkdown;
    }

    public void setRisksMarkdown(String risksMarkdown) {
        this.risksMarkdown = risksMarkdown;
    }

    public String getRollbackMarkdown() {
        return rollbackMarkdown;
    }

    public void setRollbackMarkdown(String rollbackMarkdown) {
        this.rollbackMarkdown = rollbackMarkdown;
    }

    public String getVerificationMarkdown() {
        return verificationMarkdown;
    }

    public void setVerificationMarkdown(String verificationMarkdown) {
        this.verificationMarkdown = verificationMarkdown;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
