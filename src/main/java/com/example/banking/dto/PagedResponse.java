package com.example.banking.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> items,
        int offset,
        int limit,
        long totalItems
) {}
