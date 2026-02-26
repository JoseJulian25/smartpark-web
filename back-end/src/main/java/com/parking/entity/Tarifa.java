package com.parking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_vehiculo_id", nullable = false, unique = true)
    private TipoVehiculo tipoVehiculo;

    @NotNull
    @Min(1)
    @Column(name = "minutos_fraccion", nullable = false)
    private Integer minutosFraccion;
    
    @NotNull
    @Min(0)
    @Column(name = "minutos_tolerancia", nullable = false)
    private Integer minutosTolerancia;
    
    @NotNull
    @Min(0)
    @Column(name = "minutos_minimo", nullable = false)
    private Integer minutosMinimo;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "precio_por_fraccion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorFraccion;
    
    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
