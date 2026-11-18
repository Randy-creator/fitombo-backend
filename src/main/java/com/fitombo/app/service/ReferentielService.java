package com.fitombo.app.service;

import com.fitombo.app.dto.EspeceDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.RaceDto;
import com.fitombo.app.repository.EspeceRepository;
import com.fitombo.app.repository.RaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferentielService {

    private final EspeceRepository especes;
    private final RaceRepository races;

    public PageResult<EspeceDto> listerEspeces(Pageable pageable) {
        return PageResult.from(especes.findActives(pageable));
    }

    public PageResult<RaceDto> listerRaces(String code, Pageable pageable) {
        if (!especes.existsByCodeAndActifTrue(code)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Espèce introuvable");
        }
        return PageResult.from(races.findActivesParEspece(code, pageable));
    }
}
