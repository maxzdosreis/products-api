package com.maxzdosreis.products_api.repository.spec;

import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.model.Product.ProductType;
import com.maxzdosreis.products_api.model.enums.MatchMode;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpecification {

    public ProductSpecification() {}

    public static Specification<Product> hasType(ProductType type) {
        if(type == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("type"), type);
    }

    public static Specification<Product> typeIn(List<ProductType> types) {
        if (types == null || types.isEmpty()) return null;

        return (root, query, cb) ->
                root.get("type").in(types);
    }

    public static Specification<Product> isEnabled(Boolean enabled) {
        if (enabled == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> {
            Join<Object, Object> category =  root.join("category");
            return cb.equal(category.get("id"), categoryId);
        };
    }

    public static Specification<Product> hasStockIssues(Boolean stockIssues) {
        if (Boolean.FALSE.equals(stockIssues) || stockIssues == null) return null;
        return (root, query, cb) -> cb.or(
                cb.and(
                        cb.isNotNull(root.get("maxStock")),
                        cb.isNotNull(root.get("currentStock")),
                        cb.greaterThan(root.get("currentStock"), root.get("maxStock"))
                ),
                cb.and(
                        cb.isNotNull(root.get("minStock")),
                        cb.isNotNull(root.get("currentStock")),
                        cb.lessThan(root.get("currentStock"), root.get("minStock"))
                )
        );
    }

//    public static Specification<Product> nameContains(String name) {
//        if (name == null || name.trim().isEmpty()) return null;
//        return (root, query, cb) ->
//                cb.like(
//                        cb.lower(root.get("name")),
//                        "%" + name.toLowerCase() + "%"
//                );
//    }

    public static Specification<Product> nameLike(String name, MatchMode mode) {
        if (name == null || name.trim().isEmpty()) return null;

        String value = name.toLowerCase().trim();
        MatchMode effectiveMode = mode != null ? mode : MatchMode.CONTAINS;

        String pattern = switch (effectiveMode) {
            case STARTS_WITH -> value + "%";
            case ENDS_WITH -> "%" + value;
            case CONTAINS -> "%" + value + "%";
        };

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("salePrice"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("salePrice"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("salePrice"), max);
            }
        };
    }

    public static Specification<Product> isInStock(Boolean inStock) {
        if (inStock == null) return null;

        return (root, query, cb) ->
                inStock
                        ? cb.greaterThan(root.get("currentStock"), BigDecimal.ZERO)
                        : cb.equal(root.get("currentStock"), BigDecimal.ZERO);
    }
}
