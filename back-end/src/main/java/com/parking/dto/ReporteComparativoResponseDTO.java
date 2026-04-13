package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteComparativoResponseDTO {

    private String titulo;
    private String unidad;
    private String periodoActual;
    private String periodoComparado;
    private List<ReporteComparativoItemDTO> items;
}