package com.parking.entity;

import java.math.BigDecimal;
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
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_ticket", unique = true, nullable = false)
    private String codigoTicket;
    
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
    
    @Column(name = "hora_entrada", nullable = false)
    private LocalDateTime horaEntrada;
    
    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoTicket estado;
    
    @Column(name = "monto_total", precision = 10, scale = 2)
    private BigDecimal montoTotal;
    
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
