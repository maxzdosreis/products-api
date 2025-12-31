package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.data.dto.PasswordResetConfirmDTO;
import com.maxzdosreis.products_api.data.dto.PasswordResetRequestDTO;
import com.maxzdosreis.products_api.exception.TooManyResetAttempsException;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.TooManyListenersException;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping(value = "/forgot-password",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO request) throws TooManyResetAttempsException {
        passwordResetService.initiatePasswordReset(request.getEmail());

        return ResponseEntity.ok("Se o email existir em nossa base, um link de reset foi enviado.");
    }

    @GetMapping(value = "/reset-password",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<String> validateResetToken(@RequestParam String token) {
        passwordResetService.validateToken(token);

        return ResponseEntity.ok("Token válido! Você pode redefinir sua senha.");
    }

    @PostMapping(value = "/reset-password",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @Valid @RequestBody PasswordResetConfirmDTO request) throws TooManyResetAttempsException {
        passwordResetService.confirmPasswordReset(token, request.getNewPassword());

        return ResponseEntity.ok("Senha alterado com sucesso!");
    }
}
