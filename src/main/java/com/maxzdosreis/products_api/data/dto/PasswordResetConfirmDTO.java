package com.maxzdosreis.products_api.data.dto;

import jakarta.validation.constraints.NotBlank;
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
public class PasswordResetConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //Acredito que o melhor seja utilizar o token como param
    //@NotBlank(message = "Token é obrigatório")
    //private String token;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String newPassword;
}
