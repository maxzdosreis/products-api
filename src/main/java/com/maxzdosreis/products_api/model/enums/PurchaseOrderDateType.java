package com.maxzdosreis.products_api.model.enums;

public enum PurchaseOrderDateType {

    CREATED ("createdAt"),
    UPDATED("updatedAt"),
    CONFIRMED("confirmedAt"),
    RECEIVED("receivedAt");

    private final String fieldName;

    PurchaseOrderDateType(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
