package com.maxzdosreis.products_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items",
    indexes = {
        @Index(name = "idx_poi_order", columnList = "purchase_orders_id"),
        @Index(name = "idx_poi_product", columnList = "product_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id_poi")
@ToString(exclude = {"purchaseOrder", "product"})
public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_poi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "poi_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "received_quantity ", precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    public BigDecimal getSubTotal() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(unitPrice);
    }

    public boolean isFullyReceived() {
        if (receivedQuantity == null) return false;
        return receivedQuantity.compareTo(quantity) >= 0;
    }
}
