package com.maxzdosreis.products_api.controller;

import com.maxzdosreis.products_api.data.dto.CategoryDTO;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/categories")
@RestController
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryDTO));
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE})
    public ResponseEntity<PagedModel<EntityModel<CategoryDTO>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }

    @GetMapping(value = "/findCategoryByName/{name}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<PagedModel<EntityModel<CategoryDTO>>> findByName(
            @PathVariable("name") String name,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Min(1) @Max(100) Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));
        return ResponseEntity.ok(categoryService.findByName(name, pageable));
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    public ResponseEntity<CategoryDTO> findById(@PathVariable("id") Long id){
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @PutMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @PatchMapping(value = "/{id}/enable",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> enableCategory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.enableCategory(id));
    }

    @PatchMapping(value = "/{id}/disable",
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, CustomMediaTypes.APPLICATION_YAML_VALUE}
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryDTO> disableProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(categoryService.disableCategory(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
