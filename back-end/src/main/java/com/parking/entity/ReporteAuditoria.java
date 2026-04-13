package com.parking.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reportes_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String usuario;

    @Column(nullable = false, length = 180)
    private String endpoint;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(length = 2000)
    private String parametros;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Boolean exitoso;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
