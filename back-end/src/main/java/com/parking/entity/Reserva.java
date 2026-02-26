package com.parking.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_reserva", unique = true, nullable = false)
    private String codigoReserva;
    
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String placa;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_vehiculo_id", nullable = false)
    private TipoVehiculo tipoVehiculo;
    
    @ManyToOne
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;
    
    @Column(name = "hora_inicio", nullable = false)
    private LocalDateTime horaInicio;
    
    @Column(name = "hora_fin", nullable = false)
    private LocalDateTime horaFin;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoReserva estado;
    
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
