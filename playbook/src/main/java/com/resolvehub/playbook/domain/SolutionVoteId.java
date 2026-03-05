package com.resolvehub.playbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SolutionVoteId implements Serializable {
    @Column(name = "solution_id")
    private Long solutionId;

    @Column(name = "user_id")
    private Long userId;

    public SolutionVoteId() {
    }

    public SolutionVoteId(Long solutionId, Long userId) {
        this.solutionId = solutionId;
        this.userId = userId;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolutionVoteId that)) return false;
        return Objects.equals(solutionId, that.solutionId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solutionId, userId);
    }
}
