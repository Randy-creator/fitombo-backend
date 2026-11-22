package com.fitombo.app.dto.exportation;

import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record PeriodeExport(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
  @AssertTrue
  public boolean isValide() {
    return du == null || au == null || !du.isAfter(au);
  }
}
