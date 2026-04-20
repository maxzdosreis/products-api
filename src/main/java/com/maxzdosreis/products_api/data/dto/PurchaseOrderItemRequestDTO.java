package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseOrderItemRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Id de produto é obrigatório")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", inclusive = true, message = "Quantidade deve ser maior que zero")
    @JsonProperty("quantity")
    private BigDecimal quantity;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "Preço unitário deve ser maior que zero")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
}
