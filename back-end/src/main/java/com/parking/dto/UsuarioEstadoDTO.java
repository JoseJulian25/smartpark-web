package com.parking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioEstadoDTO {

    @NotNull
    private Boolean activo;
}
