package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({ "id", "product_id", "product_name", "quantity", "unit_price", "subtotal", "received_quantity" })
public class PurchaseOrderItemResponseDTO extends RepresentationModel<PurchaseOrderItemResponseDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    @JsonProperty("received_quantity")
    private BigDecimal receivedQuantity;
}
