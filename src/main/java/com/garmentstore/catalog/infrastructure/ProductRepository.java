package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.Product;
import com.garmentstore.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndStatus(Long id, ProductStatus s);
    Optional<Product> findBySlugAndStatus(String slug, ProductStatus s);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    long countByStatus(ProductStatus status);
    long countByStatusNot(ProductStatus status);
    @Query("select p from Product p where p.status=:status and (:categoryId is null or p.category.id=:categoryId) and (:keyword is null or lower(p.name) like lower(concat('%',:keyword,'%'))) ")
    Page<Product> searchPublic(@Param("status") ProductStatus status, @Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
}