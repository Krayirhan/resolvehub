package com.resolvehub.playbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlaybookStepId implements Serializable {
    @Column(name = "playbook_id")
    private Long playbookId;

    @Column(name = "step_no")
    private Integer stepNo;

    public PlaybookStepId() {
    }

    public PlaybookStepId(Long playbookId, Integer stepNo) {
        this.playbookId = playbookId;
        this.stepNo = stepNo;
    }

    public Long getPlaybookId() {
        return playbookId;
    }

    public Integer getStepNo() {
        return stepNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaybookStepId that)) return false;
        return Objects.equals(playbookId, that.playbookId) && Objects.equals(stepNo, that.stepNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playbookId, stepNo);
    }
}
