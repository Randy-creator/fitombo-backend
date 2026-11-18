package com.fitombo.app.model.commun;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class EntiteUuid {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
}
