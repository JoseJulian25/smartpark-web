package com.parking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSerieTemporalItemDTO {

    private String periodo;
    private BigDecimal valor;
}
