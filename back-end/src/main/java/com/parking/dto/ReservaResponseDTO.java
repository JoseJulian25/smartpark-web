package com.parking.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {

    private Long id;
    private String codigoReserva;
    private String placa;
    private String tipoVehiculo;
    private Long espacioId;
    private String codigoEspacio;
    private String estado;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private String motivoCancelacion;
    private String canceladoPor;
    private String clienteNombreCompleto;
    private String clienteTelefono;
    private String clienteEmail;
    private Boolean correoEnviado;
    private LocalDateTime fechaCreacion;
}
