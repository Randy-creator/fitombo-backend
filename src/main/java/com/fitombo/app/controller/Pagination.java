package com.fitombo.app.controller;

import java.util.Set;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class Pagination {

    private Pagination() {
    }

    static Pageable stable(Pageable page, String tieBreaker, Set<String> allowedFields) {
        Sort sort = page.getSort();
        if (sort.stream().anyMatch(order -> !allowedFields.contains(order.getProperty()))) {
            throw new ExceptionApplicative(CodeErreur.TRI_INVALIDE);
        }
        if (sort.getOrderFor(tieBreaker) == null) {
            sort = sort.and(Sort.by(tieBreaker));
        }
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
    }
}
