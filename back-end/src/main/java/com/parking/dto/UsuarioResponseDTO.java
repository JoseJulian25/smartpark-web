package com.parking.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String nombre;
    private String rol;
    private Boolean activo;
    private LocalDateTime createdAt;
}
