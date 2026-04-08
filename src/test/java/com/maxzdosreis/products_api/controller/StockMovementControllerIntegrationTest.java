package com.maxzdosreis.products_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.data.dto.StockMovementRequestDto;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("StockMovement endpoints - integração")
public class StockMovementControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    StockMovementRequestDto stockMovementRequestDto;

    ProductDto productDto;

    @BeforeEach
    void setUp() {
        productDto = ProductDto.builder()
                .name("Produto Teste " + System.currentTimeMillis())
                .description("Descrição de produto teste")
                .unit("UN")
                .type(Product.ProductType.PRODUCT)
                .categoryId(1L)
                .costPrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("15.00"))
                .minStock(new BigDecimal("1.00"))
                .maxStock(new BigDecimal("100.00"))
                .requiresBatchControl(false)
                .requiresExpiryControl(false)
                .build();

        stockMovementRequestDto = StockMovementRequestDto.builder()
                .type(MovementType.ENTRADA)
                .quantity(new BigDecimal("5.000"))
                .reason("Teste")
                .build();
    }

    @Nested
    @DisplayName("PATCH /api/products/{id}/stock")
    class UpdateStock {

        @Test
        @DisplayName("deve retornar 403 para USER")
        @WithMockUser(roles = "USER")
        void updateStock_shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stockMovementRequestDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 quando não autenticado")
        void updateStock_shouldReturn403WhenUnauthenticated() throws  Exception {
            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stockMovementRequestDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para produto inexistente")
        @WithMockUser(roles = "ADMIN")
        void updateStock_shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc.perform(patch("/api/products/999999/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stockMovementRequestDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 400 quando type é nulo")
        @WithMockUser(roles = "ADMIN")
        void updateStock_shouldReturn400WhenTypeIsNull() throws Exception {
            stockMovementRequestDto = StockMovementRequestDto.builder()
                    .quantity(new BigDecimal("5.000"))
                    .reason("Teste")
                    .build();

            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stockMovementRequestDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando quantity é zero")
        @WithMockUser(roles = "ADMIN")
        void updateStock_shouldReturn400WhenQuantityIsZero() throws Exception {
            stockMovementRequestDto = StockMovementRequestDto.builder()
                    .type(MovementType.ENTRADA)
                    .quantity(BigDecimal.ZERO)
                    .reason("Teste")
                    .build();

            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stockMovementRequestDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}/stock/history")
    class StockHistory {

        @Test
        @DisplayName("deve retornar 200 para usuário autenticado")
        @WithMockUser(roles = "ADMIN")
        void stockHistory_shouldReturn200ForAuthenticatedUser() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(productDto)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/products/1/stock/history"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("deve retornar 403 quando não autenticado")
        void stockistory_should403WhenUnauthenticated() throws  Exception {
            mockMvc.perform(get("/api/products/1/stock/history"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para produto inexistente")
        @WithMockUser(roles = "USER")
        void stockHistory_shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc.perform(get("/api/products/999999/stock/history"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar estrutura paginada")
        @WithMockUser(roles = "ADMIN")
        void stockHistory_shouldReturnPagedStructure() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(productDto)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/products/1/stock/history")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }
}
