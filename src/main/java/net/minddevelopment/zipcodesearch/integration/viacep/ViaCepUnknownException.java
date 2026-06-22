package net.minddevelopment.zipcodesearch.integration.viacep;

import net.minddevelopment.zipcodesearch.integration.cep.exception.ProviderException;

public class ViaCepUnknownException extends ProviderException {
    public ViaCepUnknownException(String message) {
        super(message);
    }
}
