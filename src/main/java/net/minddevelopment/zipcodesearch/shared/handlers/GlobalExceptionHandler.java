package net.minddevelopment.zipcodesearch.shared.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import net.minddevelopment.zipcodesearch.shared.response.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.debug("Method not supported: method={} endpoint={} supported={}",
                e.getMethod(),
                request.getRequestURI(),
                e.getSupportedHttpMethods());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Method not supported for this endpoint"
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        String message = e.getConstraintViolations()
                .stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation failed: endpoint={} violations={} message={}",
                request.getRequestURI(),
                e.getConstraintViolations().size(),
                message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.BAD_REQUEST.value(),
                        message
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {

        String message = String.format(
                "Parâmetro '%s' é inválido",
                e.getName()
        );

        log.warn("Invalid param: endpoint={} param={} message={}",
                request.getRequestURI(),
                e.getName(),
                message);

        return ResponseEntity.badRequest().body(new ErrorResponse(false, HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(ZipcodeNotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            ZipcodeNotFound e
    ) {
        log.debug("Zipcode not found: zipcode={}", e.getZipcode());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.NOT_FOUND.value(),
                        "Zipcode not found"
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        log.debug("Resource not found: {}", e.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, HttpStatus.NOT_FOUND.value(), "Resource not found"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getClass().getName(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected error occurred"
                ));
    }
}
