package com.garmentstore.cart.infrastructure;

import com.garmentstore.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<CartItem> findByUserIdAndVariantId(Long userId, Long variantId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.id = :id AND c.user.id = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
