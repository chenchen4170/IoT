package com.iot.admin.exception;

import com.iot.admin.common.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackages = "com.iot.admin")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> business(BizException exception) {
        return error(exception.getErrorCode());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> responseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        HttpStatus knownStatus = HttpStatus.resolve(status);
        String message = exception.getReason() == null
                ? (knownStatus == null ? "HTTP " + status : knownStatus.getReasonPhrase())
                : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(R.error(Integer.toString(status), message));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<R<Void>> invalidRequest(Exception exception) {
        return error(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> notFound(NoResourceFoundException exception) {
        return error(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Void>> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(R.error(Integer.toString(HttpStatus.METHOD_NOT_ALLOWED.value()), "Method not allowed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> unexpected(Exception exception) {
        log.error("Unhandled request exception", exception);
        return error(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<R<Void>> error(ErrorCode errorCode) {
        HttpStatus status = switch (errorCode) {
            case BAD_REQUEST, PRODUCT_CATEGORY_PARENT_INVALID -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT, PRODUCT_CATEGORY_CODE_EXISTS -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
                .body(R.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
