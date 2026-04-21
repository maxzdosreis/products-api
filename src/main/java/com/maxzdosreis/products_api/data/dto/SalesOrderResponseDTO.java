package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.maxzdosreis.products_api.model.enums.SalesOrderStatus;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({ "id", "customer_name", "status", "total_amount", "notes", "created_at",
                    "confirmed_at", "shipped_at", "delivered_at", "items" })
public class SalesOrderResponseDTO extends RepresentationModel<SalesOrderResponseDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("status")
    private SalesOrderStatus status;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("confirmed_at")
    private LocalDateTime confirmedAt;

    @JsonProperty("shipped_at")
    private LocalDateTime shippedAt;

    @JsonProperty("delivered_at")
    private LocalDateTime deliveredAt;

    @JsonProperty("items")
    private List<SalesOrderItemResponseDTO> items;
}
