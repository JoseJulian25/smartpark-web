package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponseDTO {

    private String username;
    private String nombre;
    private String rol;
}
