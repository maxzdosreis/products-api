package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.controller.docs.AuthControllerDocs;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.data.dto.security.AccountCredentialsDTO;
import com.maxzdosreis.products_api.data.dto.security.SignInRequestDTO;
import com.maxzdosreis.products_api.data.dto.security.SignUpRequestDTO;
import com.maxzdosreis.products_api.model.User;
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

    @PostMapping(value = "/signin",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public ResponseEntity<?> signin(@Valid @RequestBody SignInRequestDTO credentials) {
        if(credentialsIsInvalid(credentials)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        var token = authService.signIn(credentials);

        if(token == null) ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        return ResponseEntity.ok().body(token);
    }

    @PutMapping(value = "/refresh/{username}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public ResponseEntity<?> refresh(
            @PathVariable("username") String username,
            @RequestHeader("Authorization") String refreshToken
    ) {
        if(parametersAreInvalid(username, refreshToken)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        var token = authService.refreshToken(username, refreshToken);
        if(token == null) ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        return ResponseEntity.ok().body(token);
    }

    @PostMapping(value = "/createUser",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @Override
    public UserResponseDTO create(@Valid @RequestBody SignUpRequestDTO credentials) {
        return authService.create(credentials);
    }

    @PutMapping("/users/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addPermission(
            @PathVariable Long id,
            @RequestParam String permission
    ) {
        userService.addPermissionToUser(id, permission);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removePermission(
            @PathVariable("id") Long id,
            @RequestParam String permission
    ) {
        userService.removePermissionToUser(id, permission);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listPermissions(@PathVariable("id") Long id){
        return ResponseEntity.ok(userService.listPermissionsToUser(id));
    }

    private boolean parametersAreInvalid(String username, String refreshToken) {
        return StringUtils.isBlank(username) || StringUtils.isBlank(refreshToken);
    }

    private boolean credentialsIsInvalid(SignInRequestDTO credentials) {
        return credentials == null ||
                StringUtils.isBlank(credentials.getPassword()) ||
                StringUtils.isBlank(credentials.getUsername());
    }
}
