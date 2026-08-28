package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "attribute_values",
       uniqueConstraints = @UniqueConstraint(name = "uk_attr_value",
               columnNames = {"attribute_id", "value"}))
public class AttributeValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Column(nullable = false, length = 200)
    private String value;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public AttributeValue() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Attribute getAttribute() { return attribute; }
    public void setAttribute(Attribute attribute) { this.attribute = attribute; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public static AttributeValueBuilder builder() { return new AttributeValueBuilder(); }

    public static class AttributeValueBuilder {
        private Long id;
        private Attribute attribute;
        private String value;
        private int displayOrder;

        public AttributeValueBuilder id(Long id) { this.id = id; return this; }
        public AttributeValueBuilder attribute(Attribute attribute) { this.attribute = attribute; return this; }
        public AttributeValueBuilder value(String value) { this.value = value; return this; }
        public AttributeValueBuilder displayOrder(int displayOrder) { this.displayOrder = displayOrder; return this; }

        public AttributeValue build() {
            AttributeValue av = new AttributeValue();
            av.setId(this.id);
            av.setAttribute(this.attribute);
            av.setValue(this.value);
            av.setDisplayOrder(this.displayOrder);
            return av;
        }
    }
}
