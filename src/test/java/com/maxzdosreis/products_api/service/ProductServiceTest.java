package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Category;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PagedResourcesAssembler;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryService categoryService;

    @Mock
    PagedResourcesAssembler<ProductDto> assembler;

    @InjectMocks
    ProductService productService;

    private Category category;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L).name("Eletrônicos").enabled(true).build();

        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook Dell I7 8GB RAM SSD")
                .unit("UN")
                .type(Product.ProductType.PRODUCT)
                .category(category)
                .costPrice(new BigDecimal("3000.00"))
                .salePrice(new BigDecimal("4500.00"))
                .minStock(new BigDecimal("2.000"))
                .maxStock(new BigDecimal("50.000"))
                .currentStock(new BigDecimal("10.000"))
                .requiresBatchControl(false)
                .requiresExpiryControl(false)
                .enabled(true)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook Dell I7 8GB RAM SSD")
                .unit("UN")
                .type(Product.ProductType.PRODUCT)
                .categoryId(1L)
                .categoryName("Eletrônicos")
                .costPrice(new BigDecimal("3000.00"))
                .salePrice(new BigDecimal("4500.00"))
                .minStock(new BigDecimal("2.000"))
                .maxStock(new BigDecimal("50.000"))
                .requiresBatchControl(false)
                .requiresExpiryControl(false)
                .build();
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("deve criar produto com sucesso")
        void shouldCreateProductSuccessfully() {
            given(productRepository.existsByName(anyString())).willReturn(false);
            given(categoryService.findEntityById(1L)).willReturn(category);
            given(productRepository.save(any(Product.class))).willReturn(product);

            ProductDto result = productService.createProduct(productDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(product.getName());
            assertThat(result.getCategoryId()).isEqualTo(category.getId());
            then(productRepository).should().save(any(Product.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando dto é nulo")
        void shouldThrowWhenDtoIsNull() {
            assertThatThrownBy(() -> productService.createProduct(null))
                    .isInstanceOf(RequiredObjectIsNullException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando nome já existe")
        void shouldThrowWhenNameAlreadyExists() {
            given(productRepository.existsByName(anyString())).willReturn(true);

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Notebook");
        }

        @Test
        @DisplayName("deve lançar exceção quando minStock maior que maxStock")
        void shouldThrowWhenMinStockGreaterThanMaxStock() {
            productDto.setMinStock(new BigDecimal("100.000"));
            productDto.setMaxStock(new BigDecimal("10.000"));
            given(productRepository.existsByName(anyString())).willReturn(false);

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("mínimo");
        }

        @Test
        @DisplayName("deve lançar exceção quando salePrice menor que costPrice")
        void shouldThrowWhenSalePriceLowerThanCostPrice() {
            productDto.setCostPrice(new BigDecimal("5000.00"));
            productDto.setSalePrice(new BigDecimal("3000.00"));
            given(productRepository.existsByName(anyString())).willReturn(false);

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("venda");
        }

        @Test
        @DisplayName("deve setar currentStock como zero na criação")
        void shouldSetCurrentStockToZeroOnCreate() {
            given(productRepository.existsByName(anyString())).willReturn(false);
            given(categoryService.findEntityById(1L)).willReturn(category);
            given(productRepository.save(any(Product.class))).willAnswer(inv -> {
                    Product p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
            });

            ProductDto result = productService.createProduct(productDto);

            assertThat(result.getCurrentStock()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar produto quando encontrado")
        void shouldReturnProductWhenFound() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductDto result = productService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(product.getName());
        }

        @Test
        @DisplayName("deve lançar exceção quando produto não encontrado")
        void shouldThrowWhenProductNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("deve atualizar produto com sucesso")
        void shouldUpdateProductSuccessfully() {
            productDto.setName("Notebook atualizado");
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(productRepository.existsByName(anyString())).willReturn(false);
            given(categoryService.findEntityById(1L)).willReturn(category);
            given(productRepository.save(any(Product.class))).willReturn(product);

            ProductDto result = productService.updateProduct(1L, productDto);

            assertThat(result).isNotNull();
            then(productRepository).should().save(any(Product.class));
        }

        @Test
        @DisplayName("deve lançar exceção quando dto é nulo")
        void shouldThrowWhenDtoIsNull() {
            assertThatThrownBy(() -> productService.updateProduct(1L, null))
                    .isInstanceOf(RequiredObjectIsNullException.class);
        }

        @Test
        @DisplayName("não deve verificar duplicidade quando nome não mudou")
        void shouldNotCheckDuplicateWhenNameUnchanged() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(categoryService.findEntityById(1L)).willReturn(category);
            given(productRepository.save(any(Product.class))).willReturn(product);

            productService.updateProduct(1L, productDto);

            then(productRepository).should(never()).existsByName(anyString());
        }
    }

    @Nested
    @DisplayName("enableProduct")
    class EnableProduct {

        @Test
        @DisplayName("deve habilitar produto e retornar dto com enabled=true")
        void shouldEnableProductAndReturnEnabledDto() {
            Product disabledProduct = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(false)
                    .build();

            Product enabledProduct = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(true)
                    .build();

            given(productRepository.findById(1L))
                    .willReturn(Optional.of(disabledProduct))
                    .willReturn(Optional.of(enabledProduct));

            ProductDto result = productService.enableProduct(1L);

            assertThat(result.getEnabled()).isTrue();
            then(productRepository).should().enableProduct(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando produto não encontrado")
        void shouldThrowWhenProductNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.enableProduct(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            then(productRepository).should(never()).enableProduct(anyLong());
        }

        @Test
        @DisplayName("não deve chamar enabledProduct quando produto já está habilitado")
        void shouldStillCallEnabledEvenIfAlreadyEnabled() {
            // O service não verificar o estado atual - sempre chama enabledProduct
            Product alreadyEnabled = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(true)
                    .build();

            given(productRepository.findById(1L))
                    .willReturn(Optional.of(alreadyEnabled))
                    .willReturn(Optional.of(alreadyEnabled));

            ProductDto result = productService.enableProduct(1L);

            assertThat(result.getEnabled()).isTrue();
            then(productRepository).should().enableProduct(1L);
        }
    }

    @Nested
    @DisplayName("disable")
    class Disable {

        @Test
        @DisplayName("deve desabilitar produto e retornar dto com enabled=false")
        void shouldDisabledProductAndReturnDisabledDto() {
            Product enabledProduct = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(true)
                    .build();

            Product disabledProduct = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(false)
                    .build();

            given(productRepository.findById(1L))
                    .willReturn(Optional.of(enabledProduct))
                    .willReturn(Optional.of(disabledProduct));

            ProductDto result = productService.disableProduct(1L);

            assertThat(result.getEnabled()).isFalse();
            then(productRepository).should().disableProduct(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando produto não encontrado")
        void shouldThrowWhenProductNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.disableProduct(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            then(productRepository).should(never()).disableProduct(anyLong());
        }

        @Test
        @DisplayName("não deve lançar exceção quando produto já está desabilitado")
        void shouldStillCallDisabledEvenIfAlreadyDisabled() {
            // Mesmo comportamente idempotente do enable
            Product alreadyDisabled = Product.builder()
                    .id(1L)
                    .name("Notebook")
                    .description("Notebook Dell I7 8GB RAM SSD")
                    .category(category)
                    .currentStock(BigDecimal.ZERO)
                    .requiresBatchControl(false)
                    .requiresExpiryControl(false)
                    .enabled(false)
                    .build();

            given(productRepository.findById(1L))
                    .willReturn(Optional.of(alreadyDisabled))
                    .willReturn(Optional.of(alreadyDisabled));

            ProductDto result = productService.disableProduct(1L);

            assertThat(result.getEnabled()).isFalse();
            then(productRepository).should().disableProduct(1L);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar produto quando encontrado")
        void shouldDeleteProductWhenFound() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productService.delete(1L);

            then(productRepository).should().delete(product);
        }

        @Test
        @DisplayName("deve lançar exceção quando produto não encontrado")
        void shouldThrowWhenProductNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
