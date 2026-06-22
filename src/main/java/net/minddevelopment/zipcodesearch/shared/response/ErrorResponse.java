package net.minddevelopment.zipcodesearch.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(example = "false")
        boolean success,
        int statusCode,
        String message
) {
}

