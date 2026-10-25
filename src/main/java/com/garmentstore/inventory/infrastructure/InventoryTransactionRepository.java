package com.garmentstore.inventory.infrastructure;

import com.garmentstore.inventory.domain.InventoryTransaction;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("""
        SELECT it FROM InventoryTransaction it
        JOIN FETCH it.product p
        JOIN FETCH it.variant v
        WHERE (:variantId IS NULL OR it.variant.id = :variantId)
          AND (:sku IS NULL OR LOWER(it.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
          AND (:transactionType IS NULL OR it.transactionType = :transactionType)
        ORDER BY it.createdAt DESC
    """)
    Page<InventoryTransaction> findTransactions(
            @Param("variantId") Long variantId,
            @Param("sku") String sku,
            @Param("transactionType") InventoryTransactionType transactionType,
            Pageable pageable
    );
}
