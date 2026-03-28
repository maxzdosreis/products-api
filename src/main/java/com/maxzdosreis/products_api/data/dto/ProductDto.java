package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.maxzdosreis.products_api.model.Product.ProductType;
import com.maxzdosreis.products_api.model.Product;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "name", "description", "unit", "type", "costPrice", "salePrice", "minStock", "maxStock", "currentStock", "requiresBatchControl", "requiresExpiryControl", "enabled"})
public class ProductDto extends RepresentationModel<ProductDto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 250, message = "Nome deve ter entre 3 e 250 caracteres")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 250, message = "Descrição deve ser entre 3 e 350 caracteres")
    @JsonProperty("description")
    private String description;

    @Size(max = 10, message = "Unidade deve ter no máximo 10 caracteres")
    @JsonProperty("unit")
    private String unit;

    @JsonProperty("type")
    private ProductType type;

    @DecimalMin(value = "0.0", inclusive = false, message = "Preço de custo deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "Preço de custo inválido")
    @JsonProperty("costPrice")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Preço de venda deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "Preço de venda inválido")
    @JsonProperty("salePrice")
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", message = "Estoque mínimo não pode ser negativo")
    @JsonProperty("minStock")
    private BigDecimal minStock;

    @DecimalMin(value = "0.0", message = "Estoque máximo não pode ser negativo")
    @JsonProperty("maxStock")
    private BigDecimal maxStock;

    @DecimalMin(value = "0.0", message = "Estoque atual não pode ser negativo")
    @JsonProperty("currentStock")
    private BigDecimal currentStock;

    @JsonProperty("requiresBatchControl")
    private Boolean requiresBatchControl;

    @JsonProperty("requiresExpiryControl")
    private Boolean requiresExpiryControl;

    @JsonProperty("enabled")
    private Boolean enabled;
}
