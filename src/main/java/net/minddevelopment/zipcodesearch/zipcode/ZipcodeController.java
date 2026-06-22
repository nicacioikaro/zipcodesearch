package net.minddevelopment.zipcodesearch.zipcode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.minddevelopment.zipcodesearch.shared.response.ErrorResponse;
import net.minddevelopment.zipcodesearch.zipcode.response.PageResponse;
import net.minddevelopment.zipcodesearch.zipcode.response.ZipcodeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zipcodes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Zipcodes", description = "Brazilian zipcode (CEP) lookup and street search")
public class ZipcodeController {
    private final ZipcodeService zipcodeService;

    @Operation(
            summary = "Search streets",
            description = "Searches for zipcodes by streets name using fuzzy search and returns paginated results."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                        schema = @Schema(implementation = PageResponse.class),
                        examples = @ExampleObject(
                        name = "Success",
                        value = """
                                {
                                 	"data": [
                                 		{
                                 			"zipcode": "95010-010",
                                 			"street": "Rua Antônio Pisani",
                                 			"complement": "",
                                 			"unit": null,
                                 			"neighborhood": "São Pelegrino",
                                 			"location": "Caxias do Sul",
                                 			"stateCode": "RS",
                                 			"state": "Rio Grande do Sul",
                                 			"region": "Sul",
                                 			"ibge": "4305108",
                                 			"gia": "",
                                 			"ddd": "54",
                                 			"siafi": "8599"
                                 		}
                                 	],
                                 	"page": 0,
                                 	"size": 50,
                                 	"totalElements": 1,
                                 	"totalPages": 1,
                                 	"last": true
                                 }
                            """
                        )
                    )

            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "status": 400,
                                      "message": "Invalid request parameters"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "405",
                    description = "HTTP method not supported for this endpoint",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "status": 405,
                                      "message": "HTTP method not supported"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "status": 500,
                                      "message": "Internal server error"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ZipcodeResponse>> getStreets(
            @RequestParam(required = true)
            @Size(min = 5, message = "Street must have at least 5 characters")
            String street,

            @RequestParam(defaultValue = "0")
            Integer page
    )
    {
        return ResponseEntity.ok(zipcodeService.getByStreet(street, page));
    }

    @GetMapping("/{zipcode}")
    @Operation(
            summary = "Search zipcode",
            description = "Returns zipcode information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zipcode found"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid zipcode",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                name = "Success",
                                value = """
                                        {
                                            "zipcode": "95010-010",
                                            "street": "Rua Antônio Pisani",
                                            "complement": "",
                                            "unit": null,
                                            "neighborhood": "São Pelegrino",
                                            "location": "Caxias do Sul",
                                            "stateCode": "RS",
                                            "state": "Rio Grande do Sul",
                                            "region": "Sul",
                                            "ibge": "4305108",
                                            "gia": "",
                                            "ddd": "54",
                                            "siafi": "8599"
                                         }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Zipcode not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                value = """
                                    {
                                      "success": false,
                                      "status": 404,
                                      "message": "Zipcode not found"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "405",
                    description = "HTTP method not supported for this endpoint",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "status": 405,
                                      "message": "HTTP method not supported"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "status": 500,
                                      "message": "Internal server error"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<ZipcodeResponse> getZipcode(
            @PathVariable
            @Pattern(
                    regexp = "\\d{8}|\\d{5}-\\d{3}",
                    message = "Zipcode must be 8 digits, with or without a hyphen."
            )
            String zipcode
    ){
        return ResponseEntity.ok(zipcodeService.getByCep(zipcode));
    }
}
