package com.garmentstore.common.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_actor", columnList = "actor_user_id"),
        @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_logs_action_time", columnList = "action_type, created_at")
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role", length = 80)
    private String actorRole;

    @Column(name = "action_type", nullable = false, length = 120)
    private String actionType;

    @Column(name = "entity_type", nullable = false, length = 120)
    private String entityType;

    @Column(name = "entity_id", length = 120)
    private String entityId;

    @Column(name = "before_json", columnDefinition = "json")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "json")
    private String afterJson;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getBeforeJson() { return beforeJson; }
    public void setBeforeJson(String beforeJson) { this.beforeJson = beforeJson; }
    public String getAfterJson() { return afterJson; }
    public void setAfterJson(String afterJson) { this.afterJson = afterJson; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }

    public static class AuditLogBuilder {
        private Long id;
        private Long actorUserId;
        private String actorRole;
        private String actionType;
        private String entityType;
        private String entityId;
        private String beforeJson;
        private String afterJson;
        private String ipAddress;

        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder actorUserId(Long actorUserId) { this.actorUserId = actorUserId; return this; }
        public AuditLogBuilder actorRole(String actorRole) { this.actorRole = actorRole; return this; }
        public AuditLogBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public AuditLogBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public AuditLogBuilder entityId(String entityId) { this.entityId = entityId; return this; }
        public AuditLogBuilder beforeJson(String beforeJson) { this.beforeJson = beforeJson; return this; }
        public AuditLogBuilder afterJson(String afterJson) { this.afterJson = afterJson; return this; }
        public AuditLogBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }

        public AuditLog build() {
            AuditLog a = new AuditLog();
            a.setId(this.id);
            a.setActorUserId(this.actorUserId);
            a.setActorRole(this.actorRole);
            a.setActionType(this.actionType);
            a.setEntityType(this.entityType);
            a.setEntityId(this.entityId);
            a.setBeforeJson(this.beforeJson);
            a.setAfterJson(this.afterJson);
            a.setIpAddress(this.ipAddress);
            return a;
        }
    }
}
