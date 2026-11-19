package com.iot.admin.exception;

public enum ErrorCode {
    BAD_REQUEST("400", "Invalid request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not found"),
    CONFLICT("409", "Conflict"),
    INTERNAL_ERROR("500", "Internal server error"),

    //PRODUCT_CATEGORY_xxxx
    PRODUCT_CATEGORY_CODE_EXISTS("PRODUCT_CATEGORY_0001", "Category code already exists"),
    PRODUCT_CATEGORY_PARENT_INVALID("PRODUCT_CATEGORY_0002", "Parent category not found"),
    PRODUCT_CATEGORY_NOT_FOUND("PRODUCT_CATEGORY_0003", "Product category not found"),
    PRODUCT_CATEGORY_RELATED_DATA("PRODUCT_CATEGORY_0004", "Product category has related data, cannot delete"),

    //PRODUCT_xxxx

    ;



    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
