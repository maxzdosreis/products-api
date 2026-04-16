package com.maxzdosreis.products_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxzdosreis.products_api.data.dto.UserRequestDTO;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.data.dto.security.SignUpRequestDTO;
import com.maxzdosreis.products_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController - integração")
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    private UserRequestDTO userRequestDto;
    private SignUpRequestDTO signUpRequestDto;

    @BeforeEach
    void setUp() {

        signUpRequestDto = SignUpRequestDTO.builder()
                .username("maxzdosreis")
                .password("654321Aa.")
                .fullname("Max Zimmermann dos Reis")
                .email("maxzdosreis@gmail.com")
                .build();

        userRequestDto = UserRequestDTO.builder()
                .userName("maxzdosreis")
                .fullName("Max Zimmermann dos Reis")
                .email("maxzdosreis@gmail.com")
                .build();
    }

    @Nested
    @DisplayName("GET /api/users")
    class FindAll {

        @Test
        @DisplayName("deve retornar 200 para usuário autenticado")
        @WithMockUser(roles = "ADMIN")
        void findAll_shoudlReturn200() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("deve retornar 403 para usuário não autenticado")
        void findAll_shouldReturn403WhenUnathenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class FindById {

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "ADMIN")
        void findById_shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/users/999999" )
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar user existente")
        @WithMockUser(roles = "ADMIN")
        void findById_shouldReturn200WhenUserExists() throws Exception {
            MvcResult created = mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpRequestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            UserResponseDTO response = objectMapper.readValue(created.getResponse().getContentAsString(), UserResponseDTO.class);

            mockMvc.perform(get("/api/users/" + response.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(signUpRequestDto.getUsername()))
                    .andExpect(jsonPath("$.id").value(response.getId()));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("deve retornar 403 para USER")
        @WithMockUser(roles = "USER")
        void update_shouldReturn403ForUserRole() throws Exception {
            UserResponseDTO userResponseDTO = UserResponseDTO.builder().fullName("Max Zimmermann dos Reis").build();
            mockMvc.perform(put("/api/users/1" )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userResponseDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "ADMIN")
        void update_shouldReturn404WhenNotFound() throws Exception {
            UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                    .userName("maxzdosreis")
                    .fullName("maxzdosreis")
                    .email("maxzdosreis@gmail.com")
                    .enabled(true)
                    .build();

            mockMvc.perform(put("/api/users/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userResponseDTO))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("deve retornar 403 para MANAGER")
        @WithMockUser(roles = "MANAGER")
        void delete_shouldReturn403ForManagerRole() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente")
        @WithMockUser(roles = "ADMIN")
        void delete_shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/users/999999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/enable e disable")
    class EnableDisable {

        @Test
        @DisplayName("deve retornar 403 para USER no enable")
        @WithMockUser(roles = "USER")
        void enable_shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(patch("/api/users/1/enable" )
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 para USER no disable")
        @WithMockUser(roles = "USER")
        void disable_shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(patch("/api/users/1/disable" )
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente no enable")
        @WithMockUser(roles = "ADMIN")
        void enable_shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(patch("/api/users/disable/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 404 para id inexistente no disable")
        @WithMockUser(roles = "ADMIN")
        void disable_shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(patch("/api/users/999999/disable"))
                    .andExpect(status().isNotFound());
        }
    }
}
