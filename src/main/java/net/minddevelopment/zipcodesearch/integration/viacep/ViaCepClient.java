package net.minddevelopment.zipcodesearch.integration.viacep;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ViaCepClient {
    private final RestTemplate restTemplate;

    public ViaCepClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ViaCepResponse getCep(String cep) {
        // ViaCep returns HTTP 200 with an "erro" flag for unknown CEPs instead of a 404,
        // so a "not found" is detected from the response body (see ViaCepProvider), not here.
        // This block only maps actual HTTP error statuses to domain exceptions.

        log.debug("viacep_request cep={}", cep);

        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        try {
            return restTemplate.getForObject(url, ViaCepResponse.class);

        } catch (HttpClientErrorException.BadRequest ex) {
            throw new InvalidCepException("Invalid CEP");

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ViaCepCepNotFound("ZipCode not found");

        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            throw new ViaCepUnavailableException("ViaCEP Service unavailable");

        } catch (HttpServerErrorException ex) {
            throw new ViaCepUnavailableException("ViaCEP server error");

        } catch (RestClientException ex) {
            throw new ViaCepUnknownException("Unexpected error calling ViaCEP");
        }
    }
}
