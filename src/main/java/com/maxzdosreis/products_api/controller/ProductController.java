package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.data.dto.StockMovementRequestDto;
import com.maxzdosreis.products_api.data.dto.StockMovementResponseDto;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.ProductService;
import com.maxzdosreis.products_api.service.StockMovementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/products")
@RestController
@Validated
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    StockMovementService stockMovementService;

    // Endpoint de criação de produto
    @PostMapping(
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto productDto) {;
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDto));
    }

    // Endpoint de busca de produtos (com paginação)
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE})
    public ResponseEntity<PagedModel<EntityModel<ProductDto>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ){
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC: Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    // Endpoint de busca de produtos por nome
    @GetMapping(value = "/findProductByName/{name}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PagedModel<EntityModel<ProductDto>>> findByName(
            @PathVariable("name") String name,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC: Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));
        return ResponseEntity.ok(productService.findByName(name,pageable));
    }

    // Endpoint de busca de produto por ID
    @GetMapping(value = "/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<ProductDto> findById(@PathVariable("id") Long id){
        return ResponseEntity.ok(productService.findById(id));
    }

    // Endpoint de alteração de produto
    @PutMapping(value = "/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto> update(@PathVariable("id") Long id, @Valid @RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    // Endpoint de ativação de produto
    @PatchMapping(value = "/{id}/enable",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto> enableProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.enableProduct(id));
    }

    // Endpoint de desativação de produto
    @PatchMapping(value = "/{id}/disable",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto> disableProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(productService.disableProduct(id));
    }

    // Endpoint de movimentação de estoque
    @PatchMapping(value = "/{id}/stock",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<StockMovementResponseDto> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequestDto request
    ) {
        return ResponseEntity.ok(stockMovementService.registerMovement(id, request));
    }

    // Endpoint de histórico de movimentações de estoque
    @GetMapping(value = "/{id}/stock/history",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PagedModel<EntityModel<StockMovementResponseDto>>> stockHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC: Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"));
        return ResponseEntity.ok(stockMovementService.findByProduct(id, pageable));
    }

    // Endpoint de exclusão de produto
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
