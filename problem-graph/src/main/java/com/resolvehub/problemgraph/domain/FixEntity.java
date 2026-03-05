package com.resolvehub.problemgraph.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "fixes")
public class FixEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public FixEntity() {
    }
}
