package net.minddevelopment.zipcodesearch.integration.viacep;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ViaCepCircuitBreakerTest {

    @Autowired
    private CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCircuit() {
        registry.circuitBreaker("viacep").reset();
    }

    @Test
    void shouldOpenCircuitAfterRepeatedFailures() {
        CircuitBreaker cb = registry.circuitBreaker("viacep");

        // run 10 failing calls directly through the circuit breaker (no retry in between)
        for (int i = 0; i < 10; i++) {
            try {
                cb.executeRunnable(() -> {
                    throw new ViaCepUnavailableException("source down");
                });
            } catch (Exception ignored) {
            }
        }

        // after enough failures, the circuit must be OPEN
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // with the circuit open, the next call fails fast without running the body
        assertThatThrownBy(() ->
                cb.executeRunnable(() -> {
                    throw new ViaCepUnavailableException("should not run");
                }))
                .isInstanceOf(CallNotPermittedException.class);
    }
}