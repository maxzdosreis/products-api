package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.controller.docs.AuthControllerDocs;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.data.dto.security.SignInRequestDTO;
import com.maxzdosreis.products_api.data.dto.security.SignUpRequestDTO;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.AuthService;
import com.maxzdosreis.products_api.service.UserService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController implements AuthControllerDocs {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // Autentica um usuário e retorna tokens JWT
    // POST /auth/signin
    @PostMapping(value = "/signin",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public ResponseEntity<?> signin(@Valid @RequestBody SignInRequestDTO credentials) {
        if(credentialsIsInvalid(credentials)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Requisição inválida");
        var token = authService.signIn(credentials);
        return ResponseEntity.ok().body(token);
    }

    // Renova o access token usando refresh token
    // PUT /auth/refresh/{username}
    @PutMapping(value = "/refresh/{username}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public ResponseEntity<?> refresh(
            @PathVariable("username") String username,
            @RequestHeader("Authorization") String refreshToken
    ) {
        if(parametersAreInvalid(username, refreshToken)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Requisição inválida");
        var token = authService.refreshToken(username, refreshToken);
        return ResponseEntity.ok().body(token);
    }

    // Registra novo usuário no sistema, caso ainda não tenha
    // POST /auth/signup
    @PostMapping(value = "/signup",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody SignUpRequestDTO credentials) {
        UserResponseDTO user = authService.create(credentials);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // Métodos auxiliares

    private boolean parametersAreInvalid(String username, String refreshToken) {
        return StringUtils.isBlank(username) || StringUtils.isBlank(refreshToken);
    }

    private boolean credentialsIsInvalid(SignInRequestDTO credentials) {
        return credentials == null ||
                StringUtils.isBlank(credentials.getPassword()) ||
                StringUtils.isBlank(credentials.getUsername());
    }
}
