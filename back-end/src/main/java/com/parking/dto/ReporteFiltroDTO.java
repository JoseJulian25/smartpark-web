package com.parking.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReporteFiltroDTO {

    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private String tipoVehiculo;
    private String estado;
    private Long usuarioId;
}
