package com.parking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIndicadorFinancieroDTO {

    private String codigo;
    private String nombre;
    private BigDecimal valor;
    private String moneda;
    private BigDecimal variacionPorcentual;
}