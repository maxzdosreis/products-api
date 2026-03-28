package com.maxzdosreis.products_api.repository;

import com.maxzdosreis.products_api.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.enabled = false WHERE c.id =:id")
    void disableCategory(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.enabled = true WHERE c.id =:id")
    void enableCategory(@Param("id") Long id);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))")
    Page<Category> findByNameContaining(String name, Pageable pageable);
}
