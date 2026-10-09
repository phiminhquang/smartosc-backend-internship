package com.example.device.service;

import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import java.util.Map;

public final class PaginationSupport {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_KEYWORD_LENGTH = 100;

    private PaginationSupport() {
    }

    public static Pageable pageRequest(
            int page,
            int size,
            String requestedSort,
            Map<String, String> allowedSorts,
            String defaultField,
            Sort.Direction defaultDirection
    ) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw invalidRequest();
        }

        String sortField = defaultField;
        Sort.Direction direction = defaultDirection;

        if (requestedSort != null && !requestedSort.isBlank()) {
            String[] parts = requestedSort.trim().split(",", -1);
            if (parts.length != 2) {
                throw invalidRequest();
            }

            sortField = parts[0].trim();
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException exception) {
                throw invalidRequest();
            }
        }

        String entityField = allowedSorts.get(sortField);
        if (entityField == null) {
            throw invalidRequest();
        }

        Sort sort = Sort.by(
                new Sort.Order(direction, entityField),
                new Sort.Order(direction, "id")
        );
        return PageRequest.of(page, size, sort);
    }

    public static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String normalized = keyword.trim();
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            throw invalidRequest();
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public static void requireAllowedValue(String value, Iterable<String> allowedValues) {
        if (value == null) {
            return;
        }

        for (String allowedValue : allowedValues) {
            if (allowedValue.equals(value)) {
                return;
            }
        }
        throw invalidRequest();
    }

    private static AppException invalidRequest() {
        return new AppException(ErrorCode.INVALID_PAGINATION_REQUEST);
    }
}
