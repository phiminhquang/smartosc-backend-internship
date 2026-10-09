package com.example.device.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {

    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(
                List.copyOf(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
