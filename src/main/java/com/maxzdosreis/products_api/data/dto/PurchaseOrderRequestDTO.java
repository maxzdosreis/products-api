package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maxzdosreis.products_api.model.PurchaseOrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseOrderRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nome do fornecedor é obrigatório")
    @Size(max = 255, message = "O nome do fornecedor deve conter no máximo 255 caracteres")
    @JsonProperty("supplier_name")
    private String supplierName;

    @Size(max = 1000, message = "As notas devem conter no máximo 1000 caracteres")
    @JsonProperty("notes")
    private String notes;

    @NotEmpty(message = "A entrada deve conter pelo menos um item")
    @Valid
    @JsonProperty("items")
    private List<PurchaseOrderItemRequestDTO> items;
}
