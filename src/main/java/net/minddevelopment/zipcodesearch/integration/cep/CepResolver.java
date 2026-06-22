package net.minddevelopment.zipcodesearch.integration.cep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minddevelopment.zipcodesearch.integration.cep.exception.AllProvidersFailedException;
import net.minddevelopment.zipcodesearch.integration.cep.exception.ProviderException;
import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CepResolver {

    private final List<CepProvider> providers;

    public CepData resolve(String cep) {
        for (CepProvider provider : providers) {
            try {
                // each provider applies its own retry + circuit breaker (Resilience4j);
                // the resolver only handles fallback between sources
                return provider.fetch(cep);   // r4j já faz retry+CB aqui dentro
            } catch (ZipcodeNotFound e) {
                // a confirmed "not found" is authoritative: the CEP doesn't exist,
                // so stop the chain instead of asking the next source
                throw e;
            } catch (ProviderException e) {
                // technical failure of this source → fall back to the next one
                log.warn("Provider {} failed after resilience handling, trying next provider", provider.name(), e);
            }
        }
        throw new AllProvidersFailedException(cep);
    }
}