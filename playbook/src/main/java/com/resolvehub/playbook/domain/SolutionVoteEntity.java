package com.resolvehub.playbook.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "solution_votes")
public class SolutionVoteEntity {
    @EmbeddedId
    private SolutionVoteId id;

    @Column(nullable = false)
    private int vote;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public SolutionVoteEntity() {
    }

    public SolutionVoteEntity(SolutionVoteId id, int vote) {
        this.id = id;
        this.vote = vote;
    }

    public SolutionVoteId getId() {
        return id;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }
}
