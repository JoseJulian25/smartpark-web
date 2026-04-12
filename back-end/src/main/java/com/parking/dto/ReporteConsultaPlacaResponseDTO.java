package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteConsultaPlacaResponseDTO {

    private String placa;
    private ReporteTablaResponseDTO tickets;
    private ReporteTablaResponseDTO reservas;
}
