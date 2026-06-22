package net.minddevelopment.zipcodesearch.integration.viacep;

import net.minddevelopment.zipcodesearch.integration.cep.exception.ProviderException;

public class ViaCepUnavailableException extends ProviderException {
    public ViaCepUnavailableException(String message) {
        super(message);
    }
}
