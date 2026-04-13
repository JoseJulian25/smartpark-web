package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFinancieroResponseDTO {

    private String titulo;
    private String periodo;
    private String moneda;
    private List<ReporteIndicadorFinancieroDTO> indicadores;
}