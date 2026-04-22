package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SalesOrderRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(max = 255, message = "O nome do cliente deve conter no máximo 255 caracteres")
    @JsonProperty("customer_name")
    private String customerName;

    @Size(max = 1000, message = "As notas devem conter no máximo 1000 caracteres")
    @JsonProperty("notes")
    private String notes;

    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    @Valid
    @JsonProperty("items")
    private List<SalesOrderItemRequestDTO> items;
}
