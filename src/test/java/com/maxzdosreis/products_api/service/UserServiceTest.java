package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.UserRequestDTO;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.mapper.UserMapper;
import com.maxzdosreis.products_api.model.User;
import com.maxzdosreis.products_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserService userService;

    private User user;
    private UserRequestDTO  userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .userName("maxzdosreis")
                .fullName("Max Zimmermann dos Reis")
                .email("maxzdosreis@gmail.com")
                .password("654321Aa.")
                .build();

        userRequestDTO = UserRequestDTO.builder()
                .userName("maxzdosreis")
                .fullName("Max Zimmermann dos Reis")
                .email("maxzdosreis@gmail.com")
                .build();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar user quando encontrado")
        void shouldReturnUserWhenFound() {
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userMapper.toDto(user))
                    .willReturn(new UserResponseDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getEnabled()
                    ));

            UserResponseDTO result = userService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getUserName()).isEqualTo(user.getUsername());
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar exceção qaundo user não encontrado")
        void shouldThrowWhenCategoryNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("deve atualizar user com sucesso")
        void shouldUpdateUserSuccessfully() {
            userRequestDTO.setFullName("Max Zimmermann dos Reis");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user))
                    .willReturn(new UserResponseDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getEnabled()
                    ));

            UserResponseDTO result = userService.updateUser(1L, userRequestDTO);

            assertThat(result).isNotNull();
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando dto é nulo")
        void shouldThrowWhenDtoIsNull() {

            assertThatThrownBy(() -> userService.updateUser(1L, null))
                    .isInstanceOf(RequiredObjectIsNullException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando user não encontrado")
        void shouldThrowWhenUserNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, userRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("enableUser")
    class EnableUser {

        @Test
        @DisplayName("deve habilitar user e retornar dto com enabled=true")
        void shouldEnabledUserSuccessfully() {
            User disableUser = User.builder()
                    .id(1L)
                    .userName("maxzdosreis")
                    .fullName("Max Zimmermann dos Reis")
                    .password("654321Aa.")
                    .enabled(false)
                    .build();

            User enabledUser = User.builder()
                    .id(1L)
                    .userName("maxzdosreis")
                    .fullName("Max Zimmermann dos Reis")
                    .password("654321Aa.")
                    .enabled(true)
                    .build();

            given(userRepository.findById(1L))
                    .willReturn(Optional.of(disableUser))
                    .willReturn(Optional.of(enabledUser));
            given(userMapper.toDto(enabledUser))
                    .willReturn(new UserResponseDTO(
                            enabledUser.getId(),
                            enabledUser.getUsername(),
                            enabledUser.getFullName(),
                            enabledUser.getEmail(),
                            enabledUser.getEnabled()
                    ));

            UserResponseDTO result = userService.enableUser(1L);

            assertThat(result.getEnabled()).isTrue();
            then(userRepository).should().enableUser(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando user não encontrado")
        void shouldthrowWhenUserNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, userRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class);

            then(userRepository).should(never()).enableUser(anyLong());
        }
    }

    @Nested
    @DisplayName("disableUser")
    class DisableUser {

        @Test
        @DisplayName("deve desabilitar o user e retornar dto com enabled=false")
        void shouldDisabledUserAndReturnDisableDto() {
            User enabledUser = User.builder()
                    .id(1L)
                    .userName("maxzdosreis")
                    .fullName("Max Zimmermann dos Reis")
                    .email("maxzdosreis@gmail.com")
                    .password("654321Aa.")
                    .enabled(true)
                    .build();

            User disableUser = User.builder()
                    .id(1L)
                    .userName("maxzdosreis")
                    .fullName("Max Zimmermann dos Reis")
                    .email("maxzdosreis@gmail.com")
                    .password("654321Aa.")
                    .enabled(false)
                    .build();



            given(userRepository.findById(1L))
                    .willReturn(Optional.of(enabledUser))
                    .willReturn(Optional.of(disableUser));
            given(userMapper.toDto(disableUser))
                    .willReturn(new UserResponseDTO(
                            disableUser.getId(),
                            disableUser.getUsername(),
                            disableUser.getFullName(),
                            disableUser.getEmail(),
                            disableUser.getEnabled()
                    ));

            UserResponseDTO result = userService.disableUser(1L);

            assertThat(result.getEnabled()).isFalse();
            then(userRepository).should().disableUser(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando user não encontrado")
        void shouldThrowWhenUserNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.disableUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar user quando encontrado")
        void shouldDeleteUserWhenFound() {
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.delete(1L);

            then(userRepository).should().delete(user);
        }

        @Test
        @DisplayName("deve lançar exceção quando user não encontrado")
        void shouldthrowWhenUserNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
