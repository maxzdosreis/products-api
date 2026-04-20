package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderStatus;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({
        "id", "supplier_name", "status", "notes", "total_amount", "created_at", "updated_at", "confirmed_at",
        "received_at", "items"
})
public class PurchaseOrderResponseDTO extends RepresentationModel<PurchaseOrderResponseDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("status")
    private PurchaseOrderStatus status;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("confirmed_at")
    private LocalDateTime confirmedAt;

    @JsonProperty("received_at")
    private LocalDateTime receivedAt;

    @JsonProperty("items")
    private List<PurchaseOrderItemResponseDTO> items;
}
