package com.maxzdosreis.products_api.repository;

import com.maxzdosreis.products_api.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name_category) LIKE LOWER(CONCAT(:name, '%'))")
    Page<Category> findByNameContaining(String name, Pageable pageable);
}
