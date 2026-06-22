package net.minddevelopment.zipcodesearch.shared.handlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.minddevelopment.zipcodesearch.integration.viacep.InvalidCepException;
import net.minddevelopment.zipcodesearch.integration.viacep.ViaCepCepNotFound;
import net.minddevelopment.zipcodesearch.integration.viacep.ViaCepUnavailableException;
import net.minddevelopment.zipcodesearch.integration.viacep.ViaCepUnknownException;
import net.minddevelopment.zipcodesearch.shared.response.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ViaCepExceptionHandler {
    @ExceptionHandler(InvalidCepException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCepException(
            InvalidCepException e,
            HttpServletRequest request
    ) {
        log.debug("Invalid zipcode: endpoint={}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(ViaCepCepNotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            ViaCepCepNotFound e,
            HttpServletRequest request
    ) {
        log.debug("Zipcode notfound: endpoint={}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(ViaCepUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ViaCepUnavailableException e) {
        log.warn("ViaCep service unavailable: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(ViaCepUnknownException.class)
    public ResponseEntity<ErrorResponse> handleUnknownErrorException(ViaCepUnknownException e) {
        log.warn("ViaCep unknown error: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        e.getMessage()
                ));
    }
}
