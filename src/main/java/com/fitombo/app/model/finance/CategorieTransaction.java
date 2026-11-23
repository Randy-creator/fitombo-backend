package com.fitombo.app.model.finance;

public enum CategorieTransaction {
  ACHAT_ANIMAUX,
  ALIMENT,
  SANTE,
  MAIN_OEUVRE,
  BATIMENT,
  TRANSPORT,
  TAXES,
  EQUIPEMENT,
  AUTRE_DEPENSE,
  VENTE_ANIMAUX(true),
  VENTE_LAIT(true),
  VENTE_OEUFS(true),
  VENTE_FUMIER(true),
  SUBVENTION(true),
  AUTRE_RECETTE(true);

  private final boolean recette;

  CategorieTransaction() {
    this(false);
  }

  CategorieTransaction(boolean recette) {
    this.recette = recette;
  }

  public String sens() {
    return recette ? "RECETTE" : "DEPENSE";
  }
}
