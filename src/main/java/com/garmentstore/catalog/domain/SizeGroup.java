package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "size_groups")
public class SizeGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public SizeGroup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static SizeGroupBuilder builder() { return new SizeGroupBuilder(); }

    public static class SizeGroupBuilder {
        private Long id;
        private String name;
        private boolean active = true;

        public SizeGroupBuilder id(Long id) { this.id = id; return this; }
        public SizeGroupBuilder name(String name) { this.name = name; return this; }
        public SizeGroupBuilder active(boolean active) { this.active = active; return this; }

        public SizeGroup build() {
            SizeGroup sg = new SizeGroup();
            sg.setId(this.id);
            sg.setName(this.name);
            sg.setActive(this.active);
            return sg;
        }
    }
}
