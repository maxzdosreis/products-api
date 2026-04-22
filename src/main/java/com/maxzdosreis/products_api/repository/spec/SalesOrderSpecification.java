package com.maxzdosreis.products_api.repository.spec;

import com.maxzdosreis.products_api.model.PurchaseOrder;
import com.maxzdosreis.products_api.model.SalesOrder;
import com.maxzdosreis.products_api.model.enums.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SalesOrderSpecification {

    private SalesOrderSpecification() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // Filtra pelo status da ordem
    public static Specification<SalesOrder> hasStatus(SalesOrderStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    // Filtra pelo nome do cliente com suporte a diferentes modos de correspondência (início, fim, contém)
    public static Specification<SalesOrder> customerNameLike(String customerName, MatchMode mode) {
        if (customerName == null || customerName.trim().isEmpty()) return null;

        String value = customerName.toLowerCase().trim();
        MatchMode effectiveMode = mode != null ? mode : MatchMode.CONTAINS;

        String pattern = switch (effectiveMode) {
            case STARTS_WITH -> value + "%";
            case ENDS_WITH -> "%" + value;
            case CONTAINS -> "%" + value + "%";
        };

        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("customerName")), pattern));
    }

    // Filtra pelo intervalo de valor total (total mínimo e total máximo)
    public static Specification<SalesOrder> totalAmountBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("totalAmount"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("totalAmount"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("totalAmount"), max);
            }
        };
    }

    // Filtra pelo intervalo de datas (início e fim) para um tipo específico de data (criação, atualização, confirmação, envio e entrega)
    public static Specification<SalesOrder> dateBetween(
            SalesOrderDateType dateType, LocalDateTime start, LocalDateTime end
    ) {
        if (dateType == null || (start == null && end == null)) return null;

        return (root, query, cb) -> {
            String field = dateType.getFieldName();

            if (start != null && end != null) {
                return cb.between(root.get(field), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get(field), start);
            } else {
                return cb.lessThanOrEqualTo(root.get(field), end);
            }
        };
    }

    // Filtra por múltiplos status da ordem
    public static Specification<SalesOrder> statusIn(List<SalesOrderStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    // Filtra por status de entrega completo
    public static Specification<SalesOrder> isFullyDelivered(Boolean fullyDelivered) {
        if (fullyDelivered == null) return null;
        return (root, query, cb) ->
                fullyDelivered
                        ? cb.equal(root.get("status"), SalesOrderStatus.DELIVERED)
                        : cb.notEqual(root.get("status"), SalesOrderStatus.DELIVERED);
    }
}
