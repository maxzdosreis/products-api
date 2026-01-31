package com.maxzdosreis.products_api.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Pattern(
        regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email deve ter um formato válido (ex: usuario@dominio.com"
    )
    private String email;
}
