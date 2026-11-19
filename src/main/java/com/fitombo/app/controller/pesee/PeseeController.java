package com.fitombo.app.controller.pesee;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.dto.pesee.PeseeDto;
import com.fitombo.app.service.pesee.PeseeService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PeseeController {
  private final PeseeService pesees;

  @PostMapping("/evenements")
  @ResponseStatus(HttpStatus.CREATED)
  public PeseeDto enregistrer(
      @UtilisateurCourant UUID utilisateurId, @Valid @RequestBody CreerPeseeRequest demande) {
    return pesees.enregistrer(utilisateurId, demande);
  }
}
