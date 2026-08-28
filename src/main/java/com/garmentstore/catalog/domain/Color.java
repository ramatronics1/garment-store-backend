package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "colors")
public class Color extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "hex_code", length = 10)
    private String hexCode;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Color() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getHexCode() { return hexCode; }
    public void setHexCode(String hexCode) { this.hexCode = hexCode; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static ColorBuilder builder() { return new ColorBuilder(); }

    public static class ColorBuilder {
        private Long id;
        private String name;
        private String code;
        private String hexCode;
        private int displayOrder;
        private boolean active = true;

        public ColorBuilder id(Long id) { this.id = id; return this; }
        public ColorBuilder name(String name) { this.name = name; return this; }
        public ColorBuilder code(String code) { this.code = code; return this; }
        public ColorBuilder hexCode(String hexCode) { this.hexCode = hexCode; return this; }
        public ColorBuilder displayOrder(int displayOrder) { this.displayOrder = displayOrder; return this; }
        public ColorBuilder active(boolean active) { this.active = active; return this; }

        public Color build() {
            Color c = new Color();
            c.setId(this.id);
            c.setName(this.name);
            c.setCode(this.code);
            c.setHexCode(this.hexCode);
            c.setDisplayOrder(this.displayOrder);
            c.setActive(this.active);
            return c;
        }
    }
}
