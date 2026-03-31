package com.maxzdosreis.products_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "name"),
    indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_type", columnList = "type"),
        @Index(name = "idx_product_enabled", columnList = "enabled"),
        @Index(name = "idx_product_category", columnList = "category_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "category")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250, unique = true)
    private String name;

    @Column(nullable = false, length = 250)
    private String description;

    @Column(length = 10)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "cost_price", precision = 19, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "min_stock", precision = 19, scale = 3)
    private BigDecimal minStock;

    @Column(name = "max_stock", precision = 19, scale = 3)
    private BigDecimal maxStock;

    @Column(name = "current_stock", precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "requires_batch_control", nullable = false)
    @Builder.Default
    private Boolean requiresBatchControl = false;

    @Column(name = "requires_expiry_control", nullable = false)
    @Builder.Default
    private Boolean requiresExpiryControl = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    public enum ProductType {
        PRODUCT, SERVICE
    }

    // Verifica se o estoque está abaixo do mínimo
    public Boolean isBelowMinStock() {
        if (currentStock == null || minStock == null) return false;
        return currentStock.compareTo(minStock) < 0;
    }

    // Verifica se o estoque está acima do máximo
    public Boolean isAboveMaxStock() {
        if (currentStock == null || maxStock == null) return false;
        return currentStock.compareTo(maxStock) > 0;
    }
}
