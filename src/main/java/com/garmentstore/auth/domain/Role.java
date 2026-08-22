package com.garmentstore.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static RoleBuilder builder() { return new RoleBuilder(); }

    public static class RoleBuilder {
        private Long id;
        private String code;
        private String name;

        public RoleBuilder id(Long id) { this.id = id; return this; }
        public RoleBuilder code(String code) { this.code = code; return this; }
        public RoleBuilder name(String name) { this.name = name; return this; }

        public Role build() {
            Role r = new Role();
            r.setId(this.id);
            r.setCode(this.code);
            r.setName(this.name);
            return r;
        }
    }
}
