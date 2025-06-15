package com.example.banking.dto;

import java.util.List;

/**
 * The type Paged response.
 *
 * @param <T> the type parameter
 */
public record PagedResponse<T>(
        List<T> items,
        int offset,
        int limit,
        long totalItems
) {}
