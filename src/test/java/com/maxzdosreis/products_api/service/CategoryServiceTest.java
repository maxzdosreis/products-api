package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.CategoryDTO;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Category;
import com.maxzdosreis.products_api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.web.PagedResourcesAssembler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
public class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    PagedResourcesAssembler<ProductDto> assembler;

    @InjectMocks
    CategoryService categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Eletrônicos")
                .description("Produtos eletrônicos em geral")
                .enabled(true)
                .build();

        categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Eletrônicos")
                .description("Produtos eletrônicos em geral")
                .build();
    }

    @Nested
    @DisplayName("createCartegory")
    class CreateCategory {

        @Test
        @DisplayName("deve criar categoria com sucesso")
        void shouldCreateCategorySuccessfully() {
            given(categoryRepository.existsByName(anyString())).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(category);

            CategoryDTO result = categoryService.createCategory(categoryDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(category.getName());
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando dto é nulo")
        void shouldThrowWhenDtoIsNull() {
            assertThatThrownBy(() -> categoryService.createCategory(null))
                    .isInstanceOf(RequiredObjectIsNullException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando o nome já existe")
        void shouldThrowWhenNameAlreadyExists() {
            given(categoryRepository.existsByName(anyString())).willReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(categoryDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Eletrônicos");

            then(categoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar categoria quando encontrado")
        void shouldReturnCategoryWhenFound() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            CategoryDTO result = categoryService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(category.getName());
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não encontrada")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("deve atualizar categoria com sucesso")
        void shouldUpdateCategorySuccessfully() {
            categoryDTO.setName("Eletrônicos Premium");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(categoryRepository.existsByName(anyString())).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(category);

            CategoryDTO result = categoryService.updateCategory(1L, categoryDTO);

            assertThat(result).isNotNull();
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando dto é nulo")
        void shouldThrowWhenDtoIsNull() {
            assertThatThrownBy(() -> categoryService.updateCategory(1L, null))
                    .isInstanceOf(RequiredObjectIsNullException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não é encontrada")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(99L, categoryDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando novo nome já existe em outra categoria")
        void shouldThrowWhenNewNameAlreadyExists() {
            CategoryDTO dtoWithDuplicateName = CategoryDTO.builder()
                    .name("Informática")
                    .description("Descrição")
                    .build();

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(categoryRepository.existsByName(anyString())).willReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(1L, dtoWithDuplicateName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Informática");
        }

        @Test
        @DisplayName("não deve verificar duplicidade quando nome não mudou")
        void shouldNotCheckDuplicateWhenNameUnchanged() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(categoryRepository.save(any(Category.class))).willReturn(category);

            categoryService.updateCategory(1L, categoryDTO);

            then(categoryRepository).should(never()).existsByName(anyString());
        }
    }

    @Nested
    @DisplayName("enableCategory")
    class EnableCategory {

        @Test
        @DisplayName("deve habilitar categoria e retornar dto com enabled=true")
        void shouldEnableCategorySuccessfully() {
            Category disabledCategory = Category.builder()
                    .id(1L)
                    .name("Eletrônicos")
                    .description("Desc")
                    .enabled(false)
                    .build();

            Category enabledCategory = Category.builder()
                    .id(1L)
                    .name("Eletrônicos")
                    .description("Desc")
                    .enabled(true)
                    .build();

            given(categoryRepository.findById(1L))
                    .willReturn(Optional.of(disabledCategory))
                    .willReturn(Optional.of(enabledCategory));

            CategoryDTO result = categoryService.enableCategory(1L);

            assertThat(result.getEnabled()).isTrue();
            then(categoryRepository).should().enableCategory(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não encontrada")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.enableCategory(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            then(categoryRepository).should(never()).enableCategory(anyLong());
        }
    }

    @Nested
    @DisplayName("disableCategory")
    class DisableCategory {

        @Test
        @DisplayName("deve desabilitar a categoria e retornar dto com enabled=false")
        void shouldDisabledCategoryAndReturnDisabledDto() {
            Category enabledCategory = Category.builder()
                    .id(1L)
                    .name("Eletrônicos")
                    .description("Desc")
                    .enabled(true)
                    .build();

            Category disabledCategory = Category.builder()
                    .id(1L)
                    .name("Eletrônicos")
                    .description("Desc")
                    .enabled(false)
                    .build();

            given(categoryRepository.findById(1L))
                    .willReturn(Optional.of(enabledCategory))
                    .willReturn(Optional.of(disabledCategory));

            CategoryDTO result = categoryService.disableCategory(1L);

            assertThat(result.getEnabled()).isFalse();
            then(categoryRepository).should().disableCategory(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não encontrada")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.disableCategory(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            then(categoryRepository).should(never()).disableCategory(anyLong());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar categoria quando encontrada")
        void shouldDeleteCategoryWhenFound() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            categoryService.delete(1L);

            then(categoryRepository).should().delete(category);
        }

        @Test
        @DisplayName("deve lançar exceção quando categoria não encontrada")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }


}
