package net.minddevelopment.zipcodesearch.zipcode.response;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
