package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "username", "fullname", "email", "enabled"})
public class UserResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 50, message = "O username deve ter entre 3 e 50 caracteres")
    @JsonProperty("username")
    private String userName;

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "O nome completo deve ter entre 3 e 100 caracteres")
    @JsonProperty("fullname")
    private String fullName;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email precisa ser válido")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email must have a valid format"
    )
    @Size(max = 100, message = "Email não pode exceder o limite de 100 caracteres")
    @JsonProperty("email")
    private String email;

    @NotNull(message = "É necessário que o usuário esteja ativo")
    @JsonProperty("enabled")
    private Boolean enabled;
}
