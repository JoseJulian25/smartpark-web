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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank
    @Size(max = 120)
    @Column(name = "cliente_nombre_completo", nullable = false, length = 120)
    private String clienteNombreCompleto;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[0-9+()\\- ]+$", message = "Telefono invalido")
    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(name = "cliente_email", nullable = false, length = 120)
    private String clienteEmail;

    @Column(name = "correo_enviado")
    private Boolean correoEnviado = false;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_vehiculo_id", nullable = false)
    private TipoVehiculo tipoVehiculo;
    
    @ManyToOne
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;
    
    @Column(name = "hora_inicio", nullable = false)
    private LocalDateTime horaInicio;
    
    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    @Size(max = 300)
    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoReserva estado;
    
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @ManyToOne
    @JoinColumn(name = "cancelado_por")
    private Usuario canceladoPor;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
