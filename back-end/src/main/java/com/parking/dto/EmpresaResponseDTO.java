package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {

    private Long id;
    private String nombre;
    private String rnc;
    private String direccion;
    private String telefono;
    private String email;
}