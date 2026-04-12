package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportesBootstrapResponseDTO {

    private String modulo;
    private List<ReporteSeccionDTO> secciones;
}
