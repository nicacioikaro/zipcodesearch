package net.minddevelopment.zipcodesearch.integration.cep;

public record CepData(
        String zipcode,
        String street,
        String complement,
        String unit,
        String neighborhood,
        String location,
        String stateCode,
        String state,
        String region,
        String ibge,
        String gia,
        String ddd,
        String siafi
) {
}
