package com.parking.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketActivoDTO {

    private String codigoTicket;
    private String placa;
    private String horaEntrada;
    private LocalDateTime fechaHoraEntrada;
}
