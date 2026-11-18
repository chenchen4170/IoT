package com.iot.admin.user.controller;

import com.iot.admin.common.R;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {AuthController.class, UserController.class, AdminUserController.class})
public class UserExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(R.error(exception.getStatusCode().value(), exception.getReason()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<R<Void>> badRequest() {
        return ResponseEntity.badRequest().body(R.error(HttpStatus.BAD_REQUEST.value(), "Invalid request"));
    }
}
