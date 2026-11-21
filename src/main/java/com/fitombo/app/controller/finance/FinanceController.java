package com.fitombo.app.controller.finance;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.finance.CreerTransactionRequest;
import com.fitombo.app.dto.finance.FiltreMarge;
import com.fitombo.app.dto.finance.MargeDto;
import com.fitombo.app.dto.finance.TransactionDto;
import com.fitombo.app.service.finance.FinanceService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exploitations/{exploitationId}")
@RequiredArgsConstructor
public class FinanceController {
  private final FinanceService finances;

  @PostMapping("/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  public TransactionDto enregistrer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @RequestBody CreerTransactionRequest demande) {
    return finances.enregistrer(utilisateurId, exploitationId, demande);
  }

  @GetMapping("/transactions")
  public PageResult<TransactionDto> lister(
      @UtilisateurCourant UUID utilisateurId, @PathVariable UUID exploitationId, Pageable page) {
    return finances.lister(utilisateurId, exploitationId, page);
  }

  @GetMapping("/marges")
  public MargeDto marge(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @ModelAttribute FiltreMarge filtre) {
    return finances.marge(utilisateurId, exploitationId, filtre);
  }
}
