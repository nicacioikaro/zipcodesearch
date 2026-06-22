package net.minddevelopment.zipcodesearch.integration.cep.exception;

public abstract class ProviderException extends RuntimeException {

    protected ProviderException(String message) {
        super(message);
    }

    protected ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}