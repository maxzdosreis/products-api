package com.maxzdosreis.products_api.controller.docs;

import com.maxzdosreis.products_api.data.dto.SalesOrderRequestDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderResponseDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderUpdateDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderPartialUpdateDTO;
import com.maxzdosreis.products_api.data.dto.SalesOrderItemRequestDTO;
import com.maxzdosreis.products_api.model.enums.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "SalesOrder", description = "Endpoints for Managing Sales Order")
public interface SalesOrderControllerDocs {

    @Operation(summary = "Adds a new SalesOrder",
            description = "Adds a new SalesOrder by passing in a JSON representation of the sales order.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> create(@Valid @RequestBody SalesOrderRequestDTO request);

    @Operation(summary = "Find All Sales Orders",
            description = "Finds All Sales Orders",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                                    )
                            }
                    ),
                    @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }

    )
    ResponseEntity<PagedModel<EntityModel<SalesOrderResponseDTO>>> findAll(
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
    );

    @Operation(summary = "Finds a Sales Order",
            description = "Find a specific salesOrder by your ID",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> findById(@PathVariable("id") Long id);

    @Operation(summary = "Updates a Sales Order",
            description = "Updates an existing sales order with new customer information, notes, and/or items. " +
                    "Only sales orders in DRAFT status can be updated. " +
                    "When items are provided, all current items are replaced with the new list and the total amount is recalculated. " +
                    "To update only metadata (customer name or notes), provide null for items field.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status, already confirmed, or empty items array",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody SalesOrderUpdateDTO request
    );

    @Operation(summary = "Partial Updates a Sales Order",
            description = "Partial Updates an existing sales order with new customer information and/or notes. " +
                    "Only DRAFT orders can be updated. " +
                    "To update only metadata (customer name or notes).",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status or already confirmed",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> partialUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody SalesOrderPartialUpdateDTO request
    );

    @Operation(summary = "Adds an Item to a Sales Order",
            description = "Adds a new item (product) to an existing sales order. " +
                    "Only DRAFT orders can have new items added. " +
                    "The product must exist and the total amount is recalculated after adding the item. " +
                    "Returns the updated sales order with all items.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Created - Item successfully added",
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status or product not found",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID or product not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> addItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody SalesOrderItemRequestDTO request
    );

    @Operation(summary = "Updates an Item in a Sales Order",
            description = "Updates an existing item (product quantity and/or price) in a sales order. " +
                    "Only DRAFT orders can have items updated. " +
                    "The item must exist in the order and the total amount is recalculated after updating. " +
                    "Returns the updated sales order with all modified items.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success - Item successfully updated",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status or item not found in order",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID or item ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> updateItem(
            @PathVariable("id") Long id,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody SalesOrderItemRequestDTO request
    );

    @Operation(summary = "Removes an Item from a Sales Order",
            description = "Removes a specific item from a sales order. " +
                    "Only DRAFT orders can have items removed. " +
                    "The item must exist in the order and the total amount is recalculated after removal. " +
                    "The order must have at least one item remaining after deletion. " +
                    "Returns the updated sales order with remaining items.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success - Item successfully removed",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status, item not found, or last item cannot be removed",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID or item ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> deleteItem(
            @PathVariable("id") Long id,
            @PathVariable("itemId") Long itemId
    );

    @Operation(summary = "Confirms a Sales Order",
            description = "Confirms a sales order by changing its status from DRAFT to CONFIRMED. Only DRAFT orders can be confirmed.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> confirm(@PathVariable("id") Long id);

    @Operation(summary = "Ships a Sales Order",
            description = "Ships a sales order by changing its status from CONFIRMED to SHIPPED. " +
                    "Only CONFIRMED orders can be shipped. " +
                    "Sets the shipment date and prepares the order for delivery.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in CONFIRMED status",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> ship(@PathVariable("id") Long id);

    @Operation(summary = "Delivers a Sales Order",
            description = "Delivers a sales order by changing its status from SHIPPED to DELIVERED. " +
                    "Only SHIPPED orders can be delivered. " +
                    "Sets the delivery date and marks the order as complete.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in SHIPPED status",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> deliver(@PathVariable("id") Long id);

    @Operation(summary = "Cancels a Sales Order",
            description = "Cancels a sales order. " +
                    "Orders that are already DELIVERED cannot be cancelled. " +
                    "Orders that are CONFIRMED or SHIPPED will trigger automatic stock reversal (ENTRADA) when cancelled. " +
                    "DRAFT orders can be cancelled without stock reversal.",
            tags = {"SalesOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SalesOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order already DELIVERED or already CANCELLED",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<SalesOrderResponseDTO> cancel(@PathVariable("id") Long id);
}
