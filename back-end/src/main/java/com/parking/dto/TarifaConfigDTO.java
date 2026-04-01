package com.parking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TarifaConfigDTO {

    @NotNull(message = "La tarifa de carro es requerida")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa de carro debe ser mayor a 0")
    private BigDecimal tarifaCarro;

    @NotNull(message = "La tarifa de moto es requerida")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa de moto debe ser mayor a 0")
    private BigDecimal tarifaMoto;

    @NotNull(message = "Los minutos por fraccion son requeridos")
    @Min(value = 1, message = "Los minutos por fraccion deben ser mayores o iguales a 1")
    private Integer minutosFraccion;

    @NotNull(message = "Los minutos de tolerancia son requeridos")
    @Min(value = 0, message = "Los minutos de tolerancia no pueden ser negativos")
    private Integer minutosTolerancia;

    @NotNull(message = "Los minutos minimos son requeridos")
    @Min(value = 0, message = "Los minutos minimos no pueden ser negativos")
    private Integer minutosMinimo;
}