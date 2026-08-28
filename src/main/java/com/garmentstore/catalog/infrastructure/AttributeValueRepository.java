package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    List<AttributeValue> findByAttributeIdOrderByDisplayOrderAsc(Long attributeId);
    boolean existsByAttributeIdAndValue(Long attributeId, String value);
    boolean existsByAttributeIdAndValueAndIdNot(Long attributeId, String value, Long id);
}
