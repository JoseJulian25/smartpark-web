package com.parking.controller.reportes;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.service.reportes.OcupacionReportService;

@RestController
@RequestMapping("/reportes/ocupacion")
public class ReportesOcupacionController {

    private final OcupacionReportService ocupacionReportService;

    public ReportesOcupacionController(OcupacionReportService ocupacionReportService) {
        this.ocupacionReportService = ocupacionReportService;
    }

    @GetMapping("/utilizacion-por-espacio")
    public ResponseEntity<ReporteTablaResponseDTO> utilizacionBasicaPorEspacio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(ocupacionReportService.obtenerUtilizacionBasicaPorEspacio(fechaDesde, fechaHasta));
    }

    @GetMapping("/tendencia-uso-espacio")
    public ResponseEntity<ReporteTablaResponseDTO> tendenciaUsoPorEspacio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad,
            @RequestParam(required = false) Integer limiteEspacios) {
        return ResponseEntity.ok(ocupacionReportService.obtenerTendenciaUsoPorEspacio(fechaDesde, fechaHasta, granularidad, limiteEspacios));
    }
}
