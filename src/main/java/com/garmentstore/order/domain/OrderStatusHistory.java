package com.garmentstore.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Immutable audit record. Every status transition appends a new row.
 * Never update or delete rows — only insert.
 */
@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    /** WHO changed it: ADMIN, SYSTEM, or CUSTOMER */
    @Column(name = "changed_by_type", nullable = false, length = 30)
    @Builder.Default
    private String changedByType = "ADMIN";

    /** user_id of the admin / customer who triggered this change. Null for SYSTEM. */
    @Column(name = "changed_by_id")
    private Long changedById;

    /** Optional short note visible on the timeline (e.g. "Dispatched via BlueDart") */
    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
