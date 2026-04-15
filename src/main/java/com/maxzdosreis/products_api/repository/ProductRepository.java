package com.maxzdosreis.products_api.repository;

import com.maxzdosreis.products_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.enabled = false WHERE p.id =:id")
    void disableProduct(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.enabled = true WHERE p.id =:id")
    void enableProduct(@Param("id") Long id);

    boolean existsByName(String name);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:name,'%'))")
    Page<Product> findProductByName(@Param("name") String name, Pageable pageable);

//    Page<Product> findByType(Product.ProductType type, Pageable pageable);
//
//    @Query("SELECT p FROM Product p WHERE p.currentStock < p.minStock AND p.minStock IS NOT NULL")
//    Page<Product> findBelowMinStock(Pageable pageable);
}
