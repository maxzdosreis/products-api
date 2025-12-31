package com.maxzdosreis.products_api.controller.docs;

import com.maxzdosreis.products_api.data.dto.PasswordResetConfirmDTO;
import com.maxzdosreis.products_api.data.dto.PasswordResetRequestDTO;
import com.maxzdosreis.products_api.exception.TooManyResetAttempsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.TooManyListenersException;

@Tag(name = "Password Reset", description = "Endpoints for reset password via email")
public interface PasswordResetControllerDocs {

    @Operation(
            summary = "Solicitar reset de senha",
            description = "Envia um email com link para redefinir a senha do usuário",
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Too Many Requests", responseCode = "429", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<String> forgotPassword(PasswordResetRequestDTO request) throws TooManyResetAttempsException;

    @Operation(
            summary = "Validar token de reset",
            description = "Verificar se o token é válido para redefinição de senha",
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<String> validateResetToken(String token);

    @Operation(
            summary = "Validar token de reset",
            description = "Verifica se o token é válido para redefinição de senha",
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<String> resetPassword(PasswordResetConfirmDTO request) throws TooManyResetAttempsException;
}
