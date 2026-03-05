package com.resolvehub.aiorchestrator.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_jobs")
public class AiJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AiJobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiJobStatus status = AiJobStatus.PENDING;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(nullable = false)
    private int retries = 0;

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

    public AiJobEntity() {
    }

    public Long getId() {
        return id;
    }

    public AiJobType getType() {
        return type;
    }

    public void setType(AiJobType type) {
        this.type = type;
    }

    public AiJobStatus getStatus() {
        return status;
    }

    public void setStatus(AiJobStatus status) {
        this.status = status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
}
