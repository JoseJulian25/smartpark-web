package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSerieTemporalResponseDTO {

    private String titulo;
    private String unidad;
    private List<ReporteSerieTemporalItemDTO> items;
}
