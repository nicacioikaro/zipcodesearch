package net.minddevelopment.zipcodesearch.zipcode.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ZipcodeRequest(

        @NotBlank(message = "Zipcode must not be blank")
        @Size(
                min = 9,
                max = 9,
                message = "Zipcode must be exactly 9 characters"
        )
        String zipcode,

        @NotNull(message = "Street must not be null")
        @Size(
                max = 150,
                message = "Street must not exceed 150 characters"
        )
        String street,

        @Size(
                max = 100,
                message = "Complement must not exceed 100 characters"
        )
        String complement,

        @Size(
                max = 50,
                message = "Unit must not exceed 50 characters"
        )
        String unit,

        @NotNull(message = "Neighborhood must not be null")
        @Size(
                max = 100,
                message = "Neighborhood must not exceed 100 characters"
        )
        String neighborhood,

        @NotBlank(message = "Location must not be blank")
        @Size(
                max = 100,
                message = "Location must not exceed 100 characters"
        )
        String location,

        @NotBlank(message = "State code must not be blank")
        @Size(
                max = 2,
                message = "State code must not exceed 2 characters"
        )
        String stateCode,

        @NotBlank(message = "State must not be blank")
        @Size(
                max = 100,
                message = "State must not exceed 100 characters"
        )
        String state,

        @NotBlank(message = "Region must not be blank")
        @Size(
                max = 100,
                message = "Region must not exceed 100 characters"
        )
        String region,

        @NotBlank(message = "IBGE code must not be blank")
        @Size(
                max = 7,
                message = "IBGE code must not exceed 7 characters"
        )
        String ibge,

        @Size(
                max = 4,
                message = "GIA must not exceed 4 characters"
        )
        String gia,

        @Size(
                max = 2,
                message = "DDD must not exceed 2 characters"
        )
        String ddd,

        @Size(
                max = 4,
                message = "SIAFI code must not exceed 4 characters"
        )
        String siafi

) {
}