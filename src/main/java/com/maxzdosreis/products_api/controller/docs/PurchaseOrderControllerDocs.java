package com.maxzdosreis.products_api.controller.docs;

import com.maxzdosreis.products_api.data.dto.*;
import com.maxzdosreis.products_api.model.enums.MatchMode;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderDateType;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderStatus;
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

@Tag(name = "PurchaseOrder", description = "Endpoints for Managing Purchase Order")
public interface PurchaseOrderControllerDocs {

    @Operation(summary = "Adds a new PurchaseOrder",
            description = "Adds a new PurchaseOrder by passing in a JSON representation of the purchase order.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> create(@Valid @RequestBody PurchaseOrderRequestDTO request);

    @Operation(summary = "Find All Purchase Orders",
            description = "Finds All Purchase Orders",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
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
    ResponseEntity<PagedModel<EntityModel<PurchaseOrderResponseDTO>>> findAll(
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
    );

    @Operation(summary = "Finds a Purchase Order",
            description = "Find a specific purchaseOrder by your ID",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> findById(@PathVariable("id") Long id);

    @Operation(summary = "Updates a Purchase Order",
            description = "Updates an existing purchase order with new supplier information, notes, and/or items. " +
                    "Only DRAFT orders can be updated. Items can only be replaced if no partial receipts exist. " +
                    "When items are provided, all current items are replaced with the new list and the total amount is recalculated. " +
                    "To update only metadata (supplier name or notes), provide null for items field. ",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status, has partially received items, or empty items array",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody PurchaseOrderUpdateDTO request
    );

    @Operation(summary = "Partial Updates a Purchase Order",
            description = "Partial Updates an existing purchase order with new supplier information and/or notes. " +
                    "Only DRAFT orders can be updated. " +
                    "To update only metadata (supplier name or notes). ",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(
                            description = "Bad Request - Order not in DRAFT status, has partially received items",
                            responseCode = "400",
                            content = @Content
                    ),
                    @ApiResponse(description = "Unauthorized - Missing ADMIN or MANAGER role", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found - Order ID not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> partialUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody PurchaseOrderPartialUpdateDTO request
    );

    @Operation(summary = "Adds an Item to a Purchase Order",
            description = "Adds a new item (product) to an existing purchase order. " +
                    "Only DRAFT orders can have new items added. " +
                    "The product must exist and the total amount is recalculated after adding the item. " +
                    "Returns the updated purchase order with all items.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Created - Item successfully added",
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
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
    ResponseEntity<PurchaseOrderResponseDTO> addItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody PurchaseOrderItemRequestDTO request
    );

    @Operation(summary = "Updates an Item in a Purchase Order",
            description = "Updates an existing item (product quantity and/or price) in a purchase order. " +
                    "Only DRAFT orders can have items updated. " +
                    "The item must exist in the order and the total amount is recalculated after updating. " +
                    "Returns the updated purchase order with all modified items.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success - Item successfully updated",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
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
    ResponseEntity<PurchaseOrderResponseDTO> updateItem(
            @PathVariable("id") Long id,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody PurchaseOrderItemRequestDTO request
    );

    @Operation(summary = "Removes an Item from a Purchase Order",
            description = "Removes a specific item from a purchase order. " +
                    "Only DRAFT orders can have items removed. " +
                    "The item must exist in the order and the total amount is recalculated after removal. " +
                    "The order must have at least one item remaining after deletion. " +
                    "Returns the updated purchase order with remaining items.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success - Item successfully removed",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
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
    ResponseEntity<PurchaseOrderResponseDTO> deleteItem(
            @PathVariable("id") Long id,
            @PathVariable("itemId") Long itemId
    );

    @Operation(summary = "Confirms a Purchase Order",
            description = "Confirms a purchase order by changing its status from DRAFT to CONFIRMED. Only DRAFT orders can be confirmed.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> confirm(@PathVariable("id") Long id);

    @Operation(summary = "Receive Items for a Purchase Order",
            description = "Receives items for a purchase order. Only CONFIRMED or PARTIALLY_RECEIVED orders can receive items.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> receiveItems(
            @PathVariable("id") Long id,
            @Valid @RequestBody List<PurchaseOrderItemRequestDTO> receivedItems
    );

    @Operation(summary = "Cancels a Purchase Order",
            description = "Cancels a purchase order. Only orders that are not RECEIVED and have no received items can be cancelled.",
            tags = {"PurchaseOrder"},
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PurchaseOrderResponseDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unathorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<PurchaseOrderResponseDTO> cancel(@PathVariable("id") Long id);
}
