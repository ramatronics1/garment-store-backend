package com.garmentstore.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_config")
public class SaleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Master on/off switch. When false the sale banner is completely hidden. */
    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(length = 200)
    private String title;

    @Column(length = 400)
    private String subtitle;

    /** Countdown target. Null = no timer shown. */
    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "badge_text", length = 80)
    private String badgeText;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /** Optional banner image (1200×400 px). */
    @Column(name = "media_url", length = 700)
    private String mediaUrl;
}
