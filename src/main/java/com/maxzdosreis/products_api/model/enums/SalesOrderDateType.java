package com.maxzdosreis.products_api.model.enums;

public enum SalesOrderDateType {

    CREATED ("createdAt"),
    UPDATED("updatedAt"),
    CONFIRMED("confirmedAt"),
    SHIPPED("shippedAt"),
    DELIVERED("deliveredAt");

    private final String fieldName;

    SalesOrderDateType(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
