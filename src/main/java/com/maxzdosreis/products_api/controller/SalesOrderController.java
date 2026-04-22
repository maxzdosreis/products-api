package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.data.dto.PurchaseOrderRequestDTO;
import com.maxzdosreis.products_api.data.dto.PurchaseOrderResponseDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderRequestDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderResponseDTO;
import com.maxzdosreis.products_api.model.enums.*;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.SalesOrderService;
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
import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@Validated
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    // Endpoint de criação de salesOrder
    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponseDTO> create(@Valid @RequestBody SalesOrderRequestDTO request) {;
        return ResponseEntity.status(HttpStatus.CREATED).body(salesOrderService.createSalesOrder(request));
    }

    @GetMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PagedModel<EntityModel<SalesOrderResponseDTO>>> findAll(
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "mode", defaultValue = "CONTAINS", required = false) MatchMode mode,
            @RequestParam(value = "status", required = false) SalesOrderStatus status,
            @RequestParam(value = "dateType", defaultValue = "CREATED", required = false) SalesOrderDateType dateType,
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "min", required = false) BigDecimal min,
            @RequestParam(value = "max", required = false) BigDecimal max,
            @RequestParam(value = "statuses", required = false) List<SalesOrderStatus> statuses,
            @RequestParam(value = "fullyDelivered", required = false) Boolean fullyDelivered,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC: Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"));

        return ResponseEntity.ok(salesOrderService.findWithFilters(
                customerName, mode, status, dateType, start, end,
                min, max, statuses, fullyDelivered, pageable));
    }

    // Endpoint de busca de salesOrder por ID
    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<SalesOrderResponseDTO> findById(@PathVariable("id") Long id){
        return ResponseEntity.ok(salesOrderService.findById(id));
    }

    // Endpoint de confirmação de salesOrder
    @PatchMapping(value = "/{id}/confirm",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponseDTO> confirm(@PathVariable("id") Long id){
        return ResponseEntity.ok(salesOrderService.confirm(id));
    }

    // Endpoint de envio de salesOrder
    @PatchMapping(value = "/{id}/ship",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponseDTO> ship(@PathVariable("id") Long id){
        return ResponseEntity.ok(salesOrderService.ship(id));
    }

    // Endpoint de entrega de salesOrder
    @PatchMapping(value = "/{id}/deliver",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponseDTO> deliver(@PathVariable("id") Long id){
        return ResponseEntity.ok(salesOrderService.deliver(id));
    }

    // Endpoint de cancelamento de salesOrder
    @PatchMapping(value = "/{id}/cancel",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesOrderResponseDTO> cancel(@PathVariable("id") Long id){
        return ResponseEntity.ok(salesOrderService.cancel(id));
    }

}
