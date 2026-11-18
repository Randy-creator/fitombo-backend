package com.fitombo.app.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Pagination {

    private Pagination() {
    }

    static Pageable stable(Pageable page, String tieBreaker, Set<String> allowedFields) {
        Sort sort = page.getSort();
        if (sort.stream().anyMatch(order -> !allowedFields.contains(order.getProperty()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Champ de tri non autorisé");
        }
        if (sort.getOrderFor(tieBreaker) == null) {
            sort = sort.and(Sort.by(tieBreaker));
        }
        return PageRequest.of(page.getPageNumber(), Math.min(page.getPageSize(), 100), sort);
    }
}
