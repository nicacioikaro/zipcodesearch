package net.minddevelopment.zipcodesearch.integration.cep;

import net.minddevelopment.zipcodesearch.integration.cep.exception.AllProvidersFailedException;
import net.minddevelopment.zipcodesearch.integration.viacep.ViaCepUnavailableException;
import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CepResolverTest {

    @Test
    void shouldThrowWhenAllProvidersFail() {
        CepProvider p = mock(CepProvider.class);
        when(p.name()).thenReturn("viacep");
        when(p.fetch(anyString())).thenThrow(new ViaCepUnavailableException("queda"));

        CepResolver resolver = new CepResolver(List.of(p));

        assertThatThrownBy(() -> resolver.resolve("01001-000"))
                .isInstanceOf(AllProvidersFailedException.class);
        verify(p, times(1)).fetch("01001-000");   // 1x agora — o retry saiu pro resilience4j
    }

    @Test
    void naoDeveRetentarQuandoNaoExiste() {
        CepProvider p = mock(CepProvider.class);
        when(p.name()).thenReturn("viacep");
        when(p.fetch(anyString())).thenThrow(new ZipcodeNotFound("99999-999"));

        CepResolver resolver = new CepResolver(List.of(p));

        assertThatThrownBy(() -> resolver.resolve("99999-999"))
                .isInstanceOf(ZipcodeNotFound.class);
        verify(p, times(1)).fetch("99999-999");
    }
}