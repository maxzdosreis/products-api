package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.data.dto.PurchaseOrderItemRequestDTO;
import com.maxzdosreis.products_api.data.dto.PurchaseOrderRequestDTO;
import com.maxzdosreis.products_api.data.dto.PurchaseOrderResponseDTO;
import com.maxzdosreis.products_api.model.enums.MatchMode;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderDateType;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderStatus;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.PurchaseOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Validated
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // Endpoint de criação de purchaseOrder
    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseOrderResponseDTO> create(@Valid @RequestBody PurchaseOrderRequestDTO request) {;
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.createPurchaseOrder(request));
    }

    @GetMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PagedModel<EntityModel<PurchaseOrderResponseDTO>>> findAll(
            @RequestParam(value = "supplierName", required = false) String supplierName,
            @RequestParam(value = "mode", defaultValue = "CONTAINS", required = false) MatchMode mode,
            @RequestParam(value = "status", required = false) PurchaseOrderStatus status,
            @RequestParam(value = "dateType", defaultValue = "CREATED", required = false) PurchaseOrderDateType dateType,
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "min", required = false) BigDecimal min,
            @RequestParam(value = "max", required = false) BigDecimal max,
            @RequestParam(value = "statuses", required = false) List<PurchaseOrderStatus> statuses,
            @RequestParam(value = "fullyReceived", required = false) Boolean fullyReceived,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC: Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"));

        return ResponseEntity.ok(purchaseOrderService.findWithFilters(
                supplierName, mode, status, dateType, start, end,
                min, max, statuses, fullyReceived, pageable));
    }

    // Endpoint de busca de purchaseOrder por ID
    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PurchaseOrderResponseDTO> findById(@PathVariable("id") Long id){
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    // Endpoint de confirmação de purchaseOrder
    @PatchMapping(value = "/{id}/confirm",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseOrderResponseDTO> confirm(@PathVariable("id") Long id){
        return ResponseEntity.ok(purchaseOrderService.confirm(id));
    }

    // Endpoint de recebimento de purchaseOrder
    @PatchMapping(value = "/{id}/receive",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseOrderResponseDTO> receiveItems(
            @PathVariable("id") Long id,
            @Valid @RequestBody List<PurchaseOrderItemRequestDTO> receivedItems
    ){
        return ResponseEntity.ok(purchaseOrderService.receiveItems(id, receivedItems));
    }

    // Endpoint de cancelamento de purchaseOrder
    @PatchMapping(value = "/{id}/cancel",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseOrderResponseDTO> cancel(@PathVariable("id") Long id){
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }

}
