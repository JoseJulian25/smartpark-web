package com.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaConfigResponseDTO {

    private BigDecimal tarifaCarro;
    private BigDecimal tarifaMoto;
    private Integer minutosFraccion;
    private Integer minutosTolerancia;
    private Integer minutosMinimo;
    private LocalDateTime actualizadoEn;
}