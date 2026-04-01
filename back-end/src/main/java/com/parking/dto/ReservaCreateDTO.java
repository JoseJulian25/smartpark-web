package com.parking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReservaCreateDTO {

    @NotBlank(message = "La placa es requerida")
    @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
    private String placa;

    @NotBlank(message = "El tipo de vehiculo es requerido")
    @Size(max = 20, message = "El tipo de vehiculo no puede exceder 20 caracteres")
    private String tipoVehiculo;

    @NotNull(message = "El id del espacio es requerido")
    private Long espacioId;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalDateTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    @Future(message = "La hora de fin debe ser futura")
    private LocalDateTime horaFin;

    @NotBlank(message = "El nombre completo del cliente es requerido")
    @Size(max = 120, message = "El nombre completo no puede exceder 120 caracteres")
    private String clienteNombreCompleto;

    @NotBlank(message = "El telefono del cliente es requerido")
    @Size(max = 20, message = "El telefono no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\- ]+$", message = "Telefono invalido")
    private String clienteTelefono;

    @NotBlank(message = "El email del cliente es requerido")
    @Email(message = "El email no es valido")
    @Size(max = 120, message = "El email no puede exceder 120 caracteres")
    private String clienteEmail;
}
