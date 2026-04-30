package com.learn.userapi.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class PageableValidator {

    // enforce reasonable page size limits
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public static Pageable validate(Pageable pageable, Set<String> allowedSortFields) {

        // cap page size
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        // validate sort fields
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            sort.forEach(order -> {
                if (!allowedSortFields.contains(order.getProperty())) {
                    throw new IllegalArgumentException(
                            "Sorting by '" + order.getProperty() +
                                    "' is not allowed. Allowed fields: " + allowedSortFields);
                }
            });
        }

        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}