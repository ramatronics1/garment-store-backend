package com.garmentstore.auth.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="roles") public class Role { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="code",nullable=false,unique=true,length=60) private String code; @Column(name="name",nullable=false,length=120) private String name; @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt; @PrePersist void prePersist(){this.createdAt=Instant.now();}}
