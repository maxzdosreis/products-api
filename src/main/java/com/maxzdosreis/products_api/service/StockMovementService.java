package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.data.dto.StockMovementRequestDto;
import com.maxzdosreis.products_api.data.dto.StockMovementResponseDto;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.model.StockMovement;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import com.maxzdosreis.products_api.repository.ProductRepository;
import com.maxzdosreis.products_api.repository.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockMovementService {

    private final Logger logger = LoggerFactory.getLogger(StockMovementService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private PagedResourcesAssembler<StockMovementResponseDto> assembler;

    @Transactional
    public StockMovementResponseDto registerMovement(Long productId, StockMovementRequestDto request) {
        logger.info("Registering {} movement for product id={}", request.getType(), productId);

        Product product = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + productId));

        BigDecimal stockBefore = product.getCurrentStock();
        BigDecimal newStock = calculateNewStock(product, request);

        validateStockLimits(product, newStock, request.getType());

        product.setCurrentStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(newStock)
                .reason(request.getReason())
                .build();

        StockMovement saved = stockMovementRepository.save(movement);
        logger.info("Movement registered: id={}, product={}, {} -> {}",
                saved.getId(), productId, stockBefore, newStock);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<StockMovementResponseDto>> findByProduct(Long productId, Pageable pageable) {
        if(!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produto não encontrado pelo id: " + productId);
        }

        var page = stockMovementRepository.findByProductId(productId, pageable).map(this::toDto);
        return assembler.toModel(page);
    }

    private BigDecimal calculateNewStock(Product product, StockMovementRequestDto request) {
        if (request.getType() == MovementType.ENTRADA) {
            return product.getCurrentStock().add(request.getQuantity());
        } else {
            return product.getCurrentStock().subtract(request.getQuantity());
        }
    }

    private void validateStockLimits(Product product, BigDecimal newStock, MovementType type) {
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    "Estoque insuficiente. Atual: " + product.getCurrentStock() +
                    ",  tentativa de saída: estoque ficaria negativo."
            );
        }
        if (type == MovementType.ENTRADA && product.getMaxStock() != null
                && newStock.compareTo(product.getMaxStock()) > 0) {
            throw new BadRequestException("Entrada excede o estoque máximo permitido (" + product.getMaxStock() + ").");
        }
    }

    private StockMovementResponseDto toDto(StockMovement entity) {
        return StockMovementResponseDto.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .stockBefore(entity.getStockBefore())
                .stockAfter(entity.getStockAfter())
                .reason(entity.getReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
