package com.resolvehub.problemgraph.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProblemTagId implements Serializable {
    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    public ProblemTagId() {
    }

    public ProblemTagId(Long problemId, String tag) {
        this.problemId = problemId;
        this.tag = tag;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProblemTagId that)) return false;
        return Objects.equals(problemId, that.problemId) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(problemId, tag);
    }
}
