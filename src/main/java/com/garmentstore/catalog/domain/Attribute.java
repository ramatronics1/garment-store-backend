package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "attributes")
public class Attribute extends BaseEntity {

    public enum Scope { PRODUCT, VARIANT, BOTH }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Scope scope = Scope.PRODUCT;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Attribute() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Scope getScope() { return scope; }
    public void setScope(Scope scope) { this.scope = scope; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static AttributeBuilder builder() { return new AttributeBuilder(); }

    public static class AttributeBuilder {
        private Long id;
        private String name;
        private Scope scope = Scope.PRODUCT;
        private boolean active = true;

        public AttributeBuilder id(Long id) { this.id = id; return this; }
        public AttributeBuilder name(String name) { this.name = name; return this; }
        public AttributeBuilder scope(Scope scope) { this.scope = scope; return this; }
        public AttributeBuilder active(boolean active) { this.active = active; return this; }

        public Attribute build() {
            Attribute a = new Attribute();
            a.setId(this.id);
            a.setName(this.name);
            a.setScope(this.scope);
            a.setActive(this.active);
            return a;
        }
    }
}
