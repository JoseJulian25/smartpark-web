package com.parking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTopNItemDTO {

    private Integer posicion;
    private String clave;
    private String descripcion;
    private BigDecimal valor;
    private String unidad;
}