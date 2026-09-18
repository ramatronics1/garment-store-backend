package com.garmentstore.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store_media")
public class StoreMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier, e.g. "hero_banner_1", "category_hero_WOMEN", "sale_banner_home" */
    @Column(name = "placement_key", nullable = false, unique = true, length = 100)
    private String placementKey;

    @Column(name = "media_url", nullable = false, length = 700)
    private String mediaUrl;

    @Column(name = "alt_text", length = 300)
    private String altText;

    /** Optional CTA destination */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /** Overlay headline shown on the banner */
    @Column(length = 200)
    private String title;

    /** Overlay subtext */
    @Column(length = 400)
    private String subtitle;

    /** Promo badge, e.g. "Up to 40% Off" */
    @Column(name = "badge_text", length = 80)
    private String badgeText;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
