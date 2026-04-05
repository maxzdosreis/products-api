package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.StockMovementRequestDto;
import com.maxzdosreis.products_api.data.dto.StockMovementResponseDto;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Category;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.model.StockMovement;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import com.maxzdosreis.products_api.repository.ProductRepository;
import com.maxzdosreis.products_api.repository.StockMovementRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementService")
public class StockMovementServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    StockMovementRepository stockMovementRepository;

    @Mock
    PagedResourcesAssembler<StockMovementResponseDto> assembler;

    @InjectMocks
    StockMovementService stockMovementService;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Geral").build();
        product = Product.builder()
                .id(1L)
                .name("Produto X")
                .description("Desc")
                .category(category)
                .currentStock(new BigDecimal("10.000"))
                .minStock(new BigDecimal("2.000"))
                .maxStock(new BigDecimal("50.000"))
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("registerMovement - ENTRADA")
    class EntradaTests {

        @Test
        @DisplayName("deve registrar entrada e aumentar estoque")
        void shouldRegisterEntradaAndIncreaseStock() {
            StockMovementRequestDto request = StockMovementRequestDto.builder()
                    .type(MovementType.ENTRADA)
                    .quantity(new BigDecimal("20.000"))
                    .reason("Compra PO-001")
                    .build();

            StockMovement saved = StockMovement.builder()
                    .id(1L).product(product).type(MovementType.ENTRADA)
                    .quantity(new BigDecimal("5.000"))
                    .stockBefore(new BigDecimal("10.000"))
                    .stockAfter(new BigDecimal("15.000"))
                    .reason("Compra PO-001")
                    .createdAt(LocalDateTime.now())
                    .build();

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(productRepository.save(any())).willReturn(product);
            given(stockMovementRepository.save(any())).willReturn(saved);

            StockMovementResponseDto result = stockMovementService.registerMovement(1L, request);

            assertThat(result.getStockAfter()).isEqualByComparingTo("15.000");
            assertThat(result.getType()).isEqualTo(MovementType.ENTRADA);
        }

        @Test
        @DisplayName("deve lançar exceção quando entrada excede estoque máximo")
        void shouldThrowWhenEntradaExceedsMaxStock() {
            Product product = Product.builder()
                    .id(1L)
                    .name("Produto X")
                    .description("Desc")
                    .category(Category.builder().id(1L).name("Geral").build())
                    .currentStock(new BigDecimal("150.000"))
                    .minStock(new BigDecimal("2.000"))
                    .maxStock(new BigDecimal("200.000"))
                    .enabled(true)
                    .build();

            StockMovementRequestDto request = StockMovementRequestDto.builder()
                    .type(MovementType.ENTRADA)
                    .quantity(new BigDecimal("100.000"))
                    .build();

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> stockMovementService.registerMovement(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("máximo");
        }
    }

    @Nested
    @DisplayName("registerMovement - SAÍDA")
    class SaidaTests {

        @Test
        @DisplayName("deve registrar saída e reduzir estoque")
        void shouldRegisterSaidaAndIncreaseStock() {
            StockMovementRequestDto request = StockMovementRequestDto.builder()
                    .type(MovementType.SAIDA)
                    .quantity(new BigDecimal("3.000"))
                    .reason("Venda SO-005")
                    .build();

            StockMovement saved = StockMovement.builder()
                    .id(2L).product(product).type(MovementType.SAIDA)
                    .quantity(new BigDecimal("3.000"))
                    .stockBefore(new BigDecimal("10.000"))
                    .stockAfter(new BigDecimal("7.000"))
                    .createdAt(LocalDateTime.now())
                    .build();

            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(productRepository.save(any())).willReturn(product);
            given(stockMovementRepository.save(any())).willReturn(saved);

            StockMovementResponseDto result = stockMovementService.registerMovement(1L, request);

            assertThat(result.getStockAfter()).isEqualByComparingTo("7.000");
        }

        @Test
        @DisplayName("deve lançar exceção quando saída deixa estoque negativo")
        void shouldThrowWhenSaidaLeavesNegativeStock() {
            StockMovementRequestDto request = StockMovementRequestDto.builder()
                    .type(MovementType.SAIDA)
                    .quantity(new BigDecimal("20.000"))
                    .build();

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> stockMovementService.registerMovement(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("insuficiente");
        }

        @Test
        @DisplayName("deve lançar exceção quando produto não encontrado")
        void shouldThrowWhenProductNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> stockMovementService.registerMovement(99L,
                    StockMovementRequestDto.builder()
                            .type(MovementType.ENTRADA).quantity(BigDecimal.ONE).build()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
