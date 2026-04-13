package com.parking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTopNResponseDTO {

    private String titulo;
    private String dimension;
    private String unidad;
    private Integer limite;
    private List<ReporteTopNItemDTO> items;
}