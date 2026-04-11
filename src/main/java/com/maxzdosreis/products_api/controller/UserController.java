package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.controller.docs.UserControllerDocs;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.data.dto.UserRequestDTO;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController implements UserControllerDocs {

    @Autowired
    private UserService userService;

    // Lista todos os usuários com paginação
    // GET /api/users
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<UserResponseDTO>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "12") Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "userName"));
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // Busca um usuário por ID
    // GET /api/users/{id}
    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable("id") Long id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    // Atualiza informações de um usuário
    // PUT /api/users/{id}
    @PutMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
        @PathVariable("id") Long id,
        @Valid @RequestBody UserRequestDTO request
    ) {
        UserResponseDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    // Ativa usuário
    // PATCH /api/users/{id}/disable
    @PatchMapping(value = "/{id}/enable",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponseDTO> enableUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.enableUser(id));
    }

    // Desativa usuário
    // PATCH /api/users/{id}/disable
    @PatchMapping(value = "/{id}/disable",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponseDTO> disableUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(userService.disableUser(id));
    }

    // Deleta um usuário
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Lista todas as permissões de um usuário
    // GET /api/users/{id}/permissions
    @GetMapping(value = "/{id}/permissions",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listPermissions(@PathVariable("id") Long id){
        return ResponseEntity.ok(userService.listPermissionsToUser(id));
    }

    // Adiciona uma permissão a um usuário
    // PUT /api/users/{id}/permissions
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addPermission(
            @PathVariable Long id,
            @RequestParam String permission
    ) {
        userService.addPermissionToUser(id, permission);
        return ResponseEntity.noContent().build();
    }

    // Remove uma permissão de um usuário
    // DELETE /api/users/{id}/permissions
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removePermission(
            @PathVariable("id") Long id,
            @RequestParam String permission
    ) {
        userService.removePermissionToUser(id, permission);
        return ResponseEntity.noContent().build();
    }
}