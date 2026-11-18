package com.fitombo.app.service;

import java.util.UUID;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.repository.AnimalRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class AnimalService {

    private final ExploitationService exploitations;
    private final AnimalRepository animaux;

    public PageResult<AnimalDto> lister(UUID utilisateurId, UUID exploitationId, StatutAnimal statut,
                                         Pageable pageable) {
        exploitations.consulter(utilisateurId, exploitationId);
        return PageResult.from(animaux.findParExploitation(exploitationId, statut, pageable));
    }
}
