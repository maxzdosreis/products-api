package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StockMovementRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Tipo é obrigatório: ENTRADA ou SAÍDA")
    @JsonProperty("type")
    private MovementType type;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
    @JsonProperty("quantity")
    private BigDecimal quantity;

    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    @JsonProperty("reason")
    private String reason;
}
