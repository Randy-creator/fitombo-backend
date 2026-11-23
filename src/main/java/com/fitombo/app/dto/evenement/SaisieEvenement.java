package com.fitombo.app.dto.evenement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fitombo.app.dto.alimentation.CreerAlimentationRequest;
import com.fitombo.app.dto.deces.CreerDecesRequest;
import com.fitombo.app.dto.mouvement.CreerMouvementRequest;
import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.dto.production.CreerProductionRequest;
import com.fitombo.app.dto.reproduction.CreerDiagnosticGestationRequest;
import com.fitombo.app.dto.reproduction.CreerMiseBasRequest;
import com.fitombo.app.dto.reproduction.CreerSaillieRequest;
import com.fitombo.app.dto.traitement.CreerTraitementRequest;
import com.fitombo.app.dto.vaccination.CreerVaccinationRequest;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = CreerMouvementRequest.class, name = "MOUVEMENT"),
  @JsonSubTypes.Type(value = CreerMiseBasRequest.class, name = "MISE_BAS"),
  @JsonSubTypes.Type(value = CreerDiagnosticGestationRequest.class, name = "DIAGNOSTIC_GESTATION"),
  @JsonSubTypes.Type(value = CreerSaillieRequest.class, name = "SAILLIE"),
  @JsonSubTypes.Type(value = CreerDecesRequest.class, name = "DECES"),
  @JsonSubTypes.Type(value = CreerAlimentationRequest.class, name = "ALIMENTATION"),
  @JsonSubTypes.Type(value = CreerVaccinationRequest.class, name = "VACCINATION"),
  @JsonSubTypes.Type(value = CreerProductionRequest.class, name = "PRODUCTION"),
  @JsonSubTypes.Type(value = CreerTraitementRequest.class, name = "TRAITEMENT"),
  @JsonSubTypes.Type(value = CreerPeseeRequest.class, name = "PESEE")
})
public interface SaisieEvenement {
  String type();

  UUID exploitationId();

  UUID animalId();

  UUID lotId();

  LocalDate dateEvenement();

  String notes();

  @AssertTrue(message = "Choisir un animal ou un lot")
  default boolean isCibleUnique() {
    return (animalId() == null) != (lotId() == null);
  }
}
