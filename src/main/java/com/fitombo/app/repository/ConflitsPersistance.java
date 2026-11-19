package com.fitombo.app.repository;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import java.util.Map;
import java.util.function.Supplier;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public final class ConflitsPersistance {
  private ConflitsPersistance() {}

  public static <T> T traduire(Supplier<T> ecriture, String contrainte, CodeErreur code) {
    return traduire(ecriture, Map.of(contrainte, code));
  }

  public static <T> T traduire(Supplier<T> ecriture, Map<String, CodeErreur> contraintes) {
    try {
      return ecriture.get();
    } catch (DataIntegrityViolationException exception) {
      if (exception.getCause() instanceof ConstraintViolationException violation
          && violation.getConstraintName() != null) {
        CodeErreur code = contraintes.get(violation.getConstraintName());
        if (code != null) {
          throw new ExceptionApplicative(code);
        }
      }
      throw exception;
    }
  }
}
