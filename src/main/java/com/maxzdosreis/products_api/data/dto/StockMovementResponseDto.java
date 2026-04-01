package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder
public class StockMovementResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("type")
    private MovementType type;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("stockBefore")
    private BigDecimal stockBefore;

    @JsonProperty("stockAfter")
    private BigDecimal stockAfter;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
