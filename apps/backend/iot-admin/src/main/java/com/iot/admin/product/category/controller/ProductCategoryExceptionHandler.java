package com.iot.admin.product.category.controller;

import com.iot.admin.response.R;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = ProductCategoryController.class)
public class ProductCategoryExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<R<Void>> duplicateCode() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(R.error(HttpStatus.CONFLICT.value(), "Category code already exists"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> responseStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(R.error(exception.getStatusCode().value(), exception.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(R.error(HttpStatus.BAD_REQUEST.value(), "Invalid request"));
    }
}
