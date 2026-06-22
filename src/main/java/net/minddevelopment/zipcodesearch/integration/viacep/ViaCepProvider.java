package net.minddevelopment.zipcodesearch.integration.viacep;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import net.minddevelopment.zipcodesearch.integration.cep.CepData;
import net.minddevelopment.zipcodesearch.integration.cep.CepProvider;
import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
@Order(1)
@RequiredArgsConstructor
public class ViaCepProvider implements CepProvider {

    private final ViaCepClient viaCepClient;
    private final ViaCepMapper mapper;

    @Override
    @CircuitBreaker(name = "viacep")
    @Retry(name = "viacep")
    public CepData fetch(String cep) {
        ViaCepResponse response = viaCepClient.getCep(cep);
        // ViaCep signals "not found" via the body flag (HTTP 200), so it's checked here
        if (response == null || response.isError()) {
            throw new ZipcodeNotFound(cep);
        }
        return mapper.toCepData(response);
    }

    @Override
    public String name() { return "viacep"; }
}