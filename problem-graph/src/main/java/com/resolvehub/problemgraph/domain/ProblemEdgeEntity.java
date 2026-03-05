package com.resolvehub.problemgraph.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "problem_edges")
public class ProblemEdgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_type", nullable = false, length = 50)
    private String fromType;

    @Column(name = "from_id", nullable = false)
    private Long fromId;

    @Column(name = "to_type", nullable = false, length = 50)
    private String toType;

    @Column(name = "to_id", nullable = false)
    private Long toId;

    @Column(name = "edge_type", nullable = false, length = 50)
    private String edgeType;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
