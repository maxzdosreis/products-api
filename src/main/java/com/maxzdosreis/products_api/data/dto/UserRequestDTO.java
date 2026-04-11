package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"username", "fullname", "email"})
public class UserRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 50, message = "O username deve ser entre 3 e 50 caracteres")
    @JsonProperty("username")
    private String userName;

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "O nome completo deve ser entre 3 e 100 caracteres")
    @JsonProperty("fullname")
    private String fullName;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email precisa ser válido")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "O email deve ter um formato válido"
    )
    @Size(max = 100, message = "O email não pode exceder o limite de 100 caracteres")
    private String email;

}
