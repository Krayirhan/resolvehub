package com.resolvehub.playbook.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "playbook_steps")
public class PlaybookStepEntity {
    @EmbeddedId
    private PlaybookStepId id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "TEXT")
    private String contentMarkdown;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    public PlaybookStepEntity() {
    }

    public PlaybookStepEntity(PlaybookStepId id, String title, String contentMarkdown) {
        this.id = id;
        this.title = title;
        this.contentMarkdown = contentMarkdown;
    }

    public PlaybookStepId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContentMarkdown() {
        return contentMarkdown;
    }
}
