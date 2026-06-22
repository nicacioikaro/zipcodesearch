package net.minddevelopment.zipcodesearch.integration.viacep;

public class InvalidCepException extends RuntimeException {
    public InvalidCepException(String message) {
        super(message);
    }
}
