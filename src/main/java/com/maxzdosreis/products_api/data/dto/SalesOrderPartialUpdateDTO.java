package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SalesOrderPartialUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 255, message = "O nome do cliente deve conter no máximo 255 caracteres")
    @JsonProperty("customer_name")
    private String customerName;

    @Size(max = 1000, message = "As notas devem conter no máximo 1000 caracteres")
    @JsonProperty("notes")
    private String notes;
}
