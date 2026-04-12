package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResumenKpiResponseDTO {

    private String titulo;
    private List<ReporteKpiDTO> kpis;
}
