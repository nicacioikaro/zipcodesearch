package net.minddevelopment.zipcodesearch.zipcode;

import net.minddevelopment.zipcodesearch.integration.cep.CepData;
import net.minddevelopment.zipcodesearch.integration.cep.CepResolver;
import net.minddevelopment.zipcodesearch.shared.ZipcodeHelper;
import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import net.minddevelopment.zipcodesearch.zipcode.mapper.ZipcodeMapper;
import net.minddevelopment.zipcodesearch.zipcode.response.ZipcodeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZipcodeServiceTest {

    @Mock
    private ZipcodeRepository repository;
    @Mock
    private CepResolver cepResolver;
    @Mock
    private ZipcodeMapper mapper;
    @Mock
    private ZipcodeHelper zipcodeHelper;

    @InjectMocks
    private ZipcodeService service;

    @Test
    void shouldReturnFromCacheWhenZipcodeExists() {
        when(zipcodeHelper.normalizeZipcode("12345678")).thenReturn("12345678");
        Zipcode cached = new Zipcode();
        when(repository.getByZipcode("12345678")).thenReturn(Optional.of(cached));
        ZipcodeResponse expected = mock(ZipcodeResponse.class);
        when(mapper.toResponse(cached)).thenReturn(expected);

        ZipcodeResponse result = service.getByCep("12345678");

        assertThat(result).isEqualTo(expected);
        verify(cepResolver, never()).resolve(anyString());   // cache hit → NÃO resolve via fontes
    }

    @Test
    void shouldResolveFromProvidersWhenNotInCache() {
        when(zipcodeHelper.normalizeZipcode("12345678")).thenReturn("12345678");
        when(repository.getByZipcode("12345678")).thenReturn(Optional.empty());

        CepData resolved = mock(CepData.class);
        when(cepResolver.resolve("12345678")).thenReturn(resolved);

        Zipcode entity = new Zipcode();
        when(mapper.toEntity(resolved)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        ZipcodeResponse expected = mock(ZipcodeResponse.class);
        when(mapper.toResponse(entity)).thenReturn(expected);

        ZipcodeResponse result = service.getByCep("12345678");

        assertThat(result).isEqualTo(expected);
        verify(cepResolver).resolve("12345678");   // resolveu via fontes
        verify(repository).save(entity);            // persistiu
    }

    @Test
    void shouldPropagateNotFoundWhenResolverThrows() {
        when(zipcodeHelper.normalizeZipcode("99999999")).thenReturn("99999999");
        when(repository.getByZipcode("99999999")).thenReturn(Optional.empty());
        when(cepResolver.resolve("99999999")).thenThrow(new ZipcodeNotFound("99999999"));

        assertThatThrownBy(() -> service.getByCep("99999999"))
                .isInstanceOf(ZipcodeNotFound.class);

        verify(repository, never()).save(any());   // não persistiu nada
    }
}