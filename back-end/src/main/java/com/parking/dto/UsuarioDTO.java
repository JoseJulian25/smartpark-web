package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioDTO {

    public interface OnCreate {}

    public interface OnUpdate {}

    @NotBlank(message = "El username es requerido")
    @Size(max = 50, message = "El username no puede exceder 50 caracteres")
    private String username;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "La password es requerida", groups = OnCreate.class)
    @Pattern(
            regexp = "^$|^.{6,255}$",
            message = "La contraseña debe tener entre 6 y 255 caracteres",
            groups = {OnCreate.class, OnUpdate.class}
    )
    private String password;

    @NotBlank(message = "El rol es requerido")
    @Size(max = 30, message = "El rol no puede exceder 30 caracteres")
    @Pattern(regexp = "^(?i)(admin|operador)$", message = "El rol debe ser admin u operador")
    private String rol;
}
