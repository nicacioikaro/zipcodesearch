package net.minddevelopment.zipcodesearch.integration.cep.exception;

public class AllProvidersFailedException extends RuntimeException {
    public AllProvidersFailedException(String cep) {
        super("All CEP providers failed for: " + cep);
    }
}
