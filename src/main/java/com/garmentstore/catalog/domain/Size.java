package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sizes",
       uniqueConstraints = @UniqueConstraint(name = "uk_size_group_code",
               columnNames = {"size_group_id", "size_code"}))
public class Size extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_group_id")
    private SizeGroup sizeGroup;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "size_code", nullable = false, length = 30)
    private String sizeCode;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Size() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SizeGroup getSizeGroup() { return sizeGroup; }
    public void setSizeGroup(SizeGroup sizeGroup) { this.sizeGroup = sizeGroup; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSizeCode() { return sizeCode; }
    public void setSizeCode(String sizeCode) { this.sizeCode = sizeCode; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static SizeBuilder builder() { return new SizeBuilder(); }

    public static class SizeBuilder {
        private Long id;
        private SizeGroup sizeGroup;
        private String name;
        private String sizeCode;
        private int sortOrder;
        private boolean active = true;

        public SizeBuilder id(Long id) { this.id = id; return this; }
        public SizeBuilder sizeGroup(SizeGroup sizeGroup) { this.sizeGroup = sizeGroup; return this; }
        public SizeBuilder name(String name) { this.name = name; return this; }
        public SizeBuilder sizeCode(String sizeCode) { this.sizeCode = sizeCode; return this; }
        public SizeBuilder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public SizeBuilder active(boolean active) { this.active = active; return this; }

        public Size build() {
            Size s = new Size();
            s.setId(this.id);
            s.setSizeGroup(this.sizeGroup);
            s.setName(this.name);
            s.setSizeCode(this.sizeCode);
            s.setSortOrder(this.sortOrder);
            s.setActive(this.active);
            return s;
        }
    }
}
