package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id_category", "name_category", "description", "enabled"})
public class CategoryDTO extends RepresentationModel<CategoryDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id_category")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @JsonProperty("name_category")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private Boolean enabled;
}
