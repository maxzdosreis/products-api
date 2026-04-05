package com.maxzdosreis.products_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.model.Product.ProductType;
import com.maxzdosreis.products_api.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProductController - integração")
public class ProductControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductService productService;

    private ProductDto validDto;

    @BeforeEach
    void setUp() {

        validDto = ProductDto.builder()
                .name("Produto Teste " + System.currentTimeMillis())
                .description("Descrição de produto teste")
                .unit("UN")
                .type(ProductType.PRODUCT)
                .categoryId(1L)
                .costPrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("15.00"))
                .minStock(new BigDecimal("1.00"))
                .maxStock(new BigDecimal("100.00"))
                .requiresBatchControl(false)
                .requiresExpiryControl(false)
                .build();
    }

    @Test
    @DisplayName("GET /api/products - deve retornar 200 para usuário autenticado")
    @WithMockUser(roles = "USER")
    void findAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/products - deve retornar 403 para usuário não autenticado")
    void findAll_shouldReturn403WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/products - deve criar produto e retornar 201")
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(validDto.getName()))
                .andExpect(jsonPath("$.categoryId").value(validDto.getCategoryId()))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 400 quando nome em branco")
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenNameBlank() throws Exception {
        validDto.setName("");
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 400 quando categoryId nulo")
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenCategoryIdNull() throws Exception {
        validDto.setCategoryId(null);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 403 para USER")
    @WithMockUser(roles = "USER")
    void create_shouldReturn403ForUserRole() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/products/{id} - deve retornar 404 para id inexistente")
    @WithMockUser(roles = "USER")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - deve retornar 403 para MANAGER")
    @WithMockUser(roles = "MANAGER")
    void delete_shouldReturn403ForManagerRole() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }
}