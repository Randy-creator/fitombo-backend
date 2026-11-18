package com.fitombo.app.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResult<T> from(Page<T> result) {
        return new PageResult<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
