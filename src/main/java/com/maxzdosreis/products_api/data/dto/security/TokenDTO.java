package com.maxzdosreis.products_api.data.dto.security;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Username não pode estar em branco")
    private String username;

    @NotNull(message = "Status de Autenticação é obrigatório")
    private Boolean authenticated;

    @NotNull(message = "Data de Criação é obrigatória")
    @PastOrPresent(message = "Data de Criação não pode ser futura")
    private Date created;

    @NotNull(message = "Data de Expiração é obrigatória")
    @Future(message = "Data de Expiração deve ser futura")
    private Date expiration;

    @NotBlank(message = "Token de Acesso é obrigatório")
    private String accessToken;

    @NotBlank(message = "Token de Atualização é obrigatório")
    private String refreshToken;
}
