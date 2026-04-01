package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReservaEstadoDTO {

    @NotBlank(message = "El estado es requerido")
    @Size(max = 30, message = "El estado no puede exceder 30 caracteres")
    private String estado;
}
