package com.garmentstore.common.domain;
import jakarta.persistence.*; import lombok.Getter; import java.time.Instant;
@Getter @MappedSuperclass
public abstract class BaseEntity {
  @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
  @Column(name="updated_at", nullable=false) private Instant updatedAt;
  @PrePersist protected void onCreate(){ Instant now=Instant.now(); this.createdAt=now; this.updatedAt=now; }
  @PreUpdate protected void onUpdate(){ this.updatedAt=Instant.now(); }
}

