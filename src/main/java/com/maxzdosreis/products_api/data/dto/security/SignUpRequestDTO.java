package com.maxzdosreis.products_api.data.dto.security;

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
public class SignUpRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 3, max = 150, message = "Nome completo deve ter entre 3 e 150 caracteres")
    private String fullname;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Pattern(
        regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email deve ter um formato válido (ex: usuario@dominio.com"
    )
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;
}
