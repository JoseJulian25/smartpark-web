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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registros_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHistorial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_evento_id", nullable = false)
    private TipoEventoHistorial tipoEvento;
    
    @NotBlank
    @Size(max = 30)
    @Column(name = "tipo_referencia", nullable = false)
    private String tipoReferencia;
    
    @NotNull
    @Column(name = "id_referencia", nullable = false)
    private Long idReferencia;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @ManyToOne
    @JoinColumn(name = "realizado_por")
    private Usuario realizadoPor;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
