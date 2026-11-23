package com.fitombo.app.dto.finance;

import java.math.BigDecimal;

public record MargeDto(
    BigDecimal recettesAriary,
    BigDecimal depensesAriary,
    BigDecimal margeAriary,
    BigDecimal poidsProduitKg,
    BigDecimal coutRevientAriaryParKg) {}
