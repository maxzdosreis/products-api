package com.maxzdosreis.products_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxzdosreis.products_api.data.dto.CategoryDTO;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CategoryController - integração")
public class CategoryControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private CategoryDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = CategoryDTO.builder()
                .name("Categoria Teste " + System.currentTimeMillis())
                .description("Descrição de categoria teste")
                .build();
    }

    @Nested
    @DisplayName("POST /api/categories")
    class Create {

        @Test
        @DisplayName("deve criar categoria e retornar 201 para ADMIN")
        @WithMockUser(roles = "ADMIN")
        void create_shouldReturn201ForAdmin() throws Exception{
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name_category").value(validDto.getName()))
                    .andExpect(jsonPath("$._links").exists());
        }

        @Test
        @DisplayName("deve criar categoria e retornar 201 para MANAGER")
        @WithMockUser(roles = "MANAGER")
        void create_shouldReturn201ForManager() throws Exception{
            validDto.setName("Categoria Manager " + System.currentTimeMillis());
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("deve retornar 403 para USER")
        void create_shouldReturn403ForUser() throws Exception{
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 quando não autenticado")
        void create_shouldReturn403WhenUnathenticated() throws Exception{
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 400 quando nome em branco")
        @WithMockUser(roles = "ADMIN")
        void create_shouldReturn400WhenNameBlank() throws Exception {
            validDto.setName("");
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando nome é nulo")
        @WithMockUser(roles = "ADMIN")
        void create_shouldReturn400WhenNameNull() throws Exception {
            validDto.setName(null);
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/categories")
    class FindAll {
        @Test
        @DisplayName("deve retornar 200 para usuário autenticado")
        @WithMockUser(roles = "USER")
        void findAll_shouldReturn200() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("deve retornar 403 para usuário não autenticado")
        void findAll_shouldReturn403WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar estrutura paginada")
        @WithMockUser(roles = "USER")
        void findAll_shouldReturnPagedStructure() throws Exception {
            mockMvc.perform(get("/api/categories")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{id}")
    class FindById {

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "USER")
        void findById_shouldReturn404WhenNotFound() throws Exception{
            mockMvc.perform(get("/api/categories/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar categoria existente")
        @WithMockUser(roles = "ADMIN")
        void findById_shouldReturn404WhenCategoryExist() throws Exception{
            MvcResult created = mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            CategoryDTO response = objectMapper.readValue(created.getResponse().getContentAsString(), CategoryDTO.class);

            mockMvc.perform(get("/api/categories/" + response.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name_category").value(validDto.getName()));
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/{id}")
    class UpdateCategory {

        @Test
        @DisplayName("deve retornar 403 para USER")
        void update_shouldReturn403ForUserRole() throws Exception{
            CategoryDTO categoryDTO = CategoryDTO.builder().name("Categoria Nova").build();
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoryDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "ADMIN")
        void update_shouldReturn404WhenNotFound() throws Exception{
            CategoryDTO categoryDTO = CategoryDTO.builder()
                    .name("Categoria Nova")
                    .description("Descrição")
                    .build();

            mockMvc.perform(put("/api/categories/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoryDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class DeleteCategory {

        @Test
        @DisplayName("deve retornar 403 para MANAGER")
        void delete_shouldReturn403ForManagerRole() throws Exception{
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "ADMIN")
        void delete_shouldReturn404WhenNotFound() throws Exception{
            mockMvc.perform(delete("/api/categories/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/categories/{id}/enable e disable")
    class EnableDisable {

        @Test
        @DisplayName("deve retornar 403 para USER no enable")
        void enable_shouldReturn403ForUserRole() throws Exception{
            mockMvc.perform(patch("/api/categories/1/enable"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 para USER no disable")
        void disable_shouldReturn403ForUserRole() throws Exception{
            mockMvc.perform(patch("/api/categories/1/disable"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente no enable")
        @WithMockUser(roles = "ADMIN")
        void enable_shouldReturn404WhenNotFound() throws Exception{
            mockMvc.perform(patch("/api/categories/999999/enable"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente no disable")
        @WithMockUser(roles = "ADMIN")
        void disable_shouldReturn404WhenNotFound() throws Exception{
            mockMvc.perform(patch("/api/categories/999999/disable"))
                    .andExpect(status().isNotFound());
        }
    }
}
