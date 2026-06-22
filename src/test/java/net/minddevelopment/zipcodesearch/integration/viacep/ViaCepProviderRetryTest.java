package net.minddevelopment.zipcodesearch.integration.viacep;

import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
class ViaCepProviderRetryTest {

    @Autowired
    private ViaCepProvider provider;

    @MockitoBean
    private ViaCepClient viaCepClient;

    @Test
    void shouldRetryThreeTimesWhenSourceFails() {
        when(viaCepClient.getCep(anyString()))
                .thenThrow(new ViaCepUnavailableException("source down"));

        assertThatThrownBy(() -> provider.fetch("01001-000"))
                .isInstanceOf(ViaCepUnavailableException.class);

        verify(viaCepClient, times(3)).getCep("01001-000");
    }

    @Test
    void shouldNotRetryWhenCepDoesNotExist() {
        ViaCepResponse error = mock(ViaCepResponse.class);
        when(error.isError()).thenReturn(true);
        when(viaCepClient.getCep(anyString())).thenReturn(error);

        assertThatThrownBy(() -> provider.fetch("99999-999"))
                .isInstanceOf(ZipcodeNotFound.class);

        verify(viaCepClient, times(1)).getCep("99999-999");
    }
}