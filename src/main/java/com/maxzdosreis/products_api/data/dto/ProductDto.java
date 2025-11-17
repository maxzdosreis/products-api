package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "name", "price", "description", "quantity"})
public class ProductDto extends RepresentationModel<ProductDto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 250, message = "Nome deve ter entre 3 e 250 caracteres")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser maior que zero")
    @JsonProperty("price")
    private Double price;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 250, message = "Descrição deve ser entre 3 e 350 caracteres")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "Quantidade deve ser maior ou igual a zero")
    @JsonProperty("quantity")
    private Integer quantity;

}
