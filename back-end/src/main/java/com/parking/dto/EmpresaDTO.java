package com.parking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String nombre;

    @NotBlank(message = "El RNC es requerido")
    @Size(max = 20, message = "El RNC no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9-]+$", message = "El RNC solo puede contener numeros y guion")
    private String rnc;

    @NotBlank(message = "La direccion es requerida")
    @Size(max = 200, message = "La direccion no puede exceder 200 caracteres")
    private String direccion;

    @NotBlank(message = "El telefono es requerido")
    @Size(max = 20, message = "El telefono no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\- ]+$", message = "Telefono invalido")
    private String telefono;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email no es valido")
    @Size(max = 120, message = "El email no puede exceder 120 caracteres")
    private String email;
}