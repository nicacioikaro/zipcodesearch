package net.minddevelopment.zipcodesearch.zipcode.response;

public record ZipcodeResponse(
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
