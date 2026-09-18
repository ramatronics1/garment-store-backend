package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "thumbnail_url", length = 700)
    private String thumbnailUrl;

    @Column(name = "banner_url", length = 700)
    private String bannerUrl;

    @Column(name = "banner_alt", length = 300)
    private String bannerAlt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 700)
    private String iconUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Category getParentCategory() { return parentCategory; }
    public void setParentCategory(Category parentCategory) { this.parentCategory = parentCategory; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getBannerAlt() { return bannerAlt; }
    public void setBannerAlt(String bannerAlt) { this.bannerAlt = bannerAlt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public static CategoryBuilder builder() { return new CategoryBuilder(); }

    public static class CategoryBuilder {
        private Long id;
        private String name;
        private String slug;
        private Category parentCategory;
        private int displayOrder;
        private boolean active;
        private String thumbnailUrl;
        private String bannerUrl;
        private String bannerAlt;
        private String description;
        private String iconUrl;

        public CategoryBuilder id(Long id) { this.id = id; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder slug(String slug) { this.slug = slug; return this; }
        public CategoryBuilder parentCategory(Category parentCategory) { this.parentCategory = parentCategory; return this; }
        public CategoryBuilder displayOrder(int displayOrder) { this.displayOrder = displayOrder; return this; }
        public CategoryBuilder active(boolean active) { this.active = active; return this; }
        public CategoryBuilder thumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }
        public CategoryBuilder bannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; return this; }
        public CategoryBuilder bannerAlt(String bannerAlt) { this.bannerAlt = bannerAlt; return this; }
        public CategoryBuilder description(String description) { this.description = description; return this; }
        public CategoryBuilder iconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }

        public Category build() {
            Category c = new Category();
            c.setId(this.id);
            c.setName(this.name);
            c.setSlug(this.slug);
            c.setParentCategory(this.parentCategory);
            c.setDisplayOrder(this.displayOrder);
            c.setActive(this.active);
            c.setThumbnailUrl(this.thumbnailUrl);
            c.setBannerUrl(this.bannerUrl);
            c.setBannerAlt(this.bannerAlt);
            c.setDescription(this.description);
            c.setIconUrl(this.iconUrl);
            return c;
        }
    }
}