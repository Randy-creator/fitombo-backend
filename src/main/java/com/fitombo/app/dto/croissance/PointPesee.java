package com.fitombo.app.dto.croissance;

import com.fitombo.app.model.evenement.MethodePesee;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PointPesee(LocalDate date, BigDecimal poidsKg, MethodePesee methode) {}
