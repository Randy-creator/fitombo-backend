package com.fitombo.app.repository;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import java.util.function.Supplier;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public final class ConflitsPersistance {
  private ConflitsPersistance() {}

  public static <T> T traduire(Supplier<T> ecriture, String contrainte, CodeErreur code) {
    try {
      return ecriture.get();
    } catch (DataIntegrityViolationException exception) {
      if (exception.getCause() instanceof ConstraintViolationException violation
          && contrainte.equals(violation.getConstraintName())) {
        throw new ExceptionApplicative(code);
      }
      throw exception;
    }
  }
}
