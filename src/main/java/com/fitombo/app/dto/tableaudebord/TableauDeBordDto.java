package com.fitombo.app.dto.tableaudebord;

import com.fitombo.app.dto.finance.MargeDto;
import java.time.LocalDate;
import java.util.List;

public record TableauDeBordDto(
    LocalDate du,
    LocalDate au,
    List<EffectifEspece> effectifs,
    long alertesDuJour,
    long objectifsEnCours,
    long deces,
    MargeDto finances) {
  public record EffectifEspece(String especeCode, long effectif) {}
}
