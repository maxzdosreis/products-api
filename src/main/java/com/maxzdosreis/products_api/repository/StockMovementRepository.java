package com.maxzdosreis.products_api.repository;

import com.maxzdosreis.products_api.model.StockMovement;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByProductIdAndType(Long productId, MovementType type, Pageable pageable);
}
