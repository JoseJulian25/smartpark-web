package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTablaResponseDTO {

    private String titulo;
    private List<String> columnas;
    private List<ReporteTablaFilaDTO> filas;
    private Long totalRegistros;
}
