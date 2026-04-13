package com.parking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteComparativoItemDTO {

    private String etiqueta;
    private BigDecimal valorActual;
    private BigDecimal valorComparado;
    private BigDecimal variacionAbsoluta;
    private BigDecimal variacionPorcentual;
}