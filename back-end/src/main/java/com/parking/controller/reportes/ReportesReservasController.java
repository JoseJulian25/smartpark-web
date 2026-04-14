package com.parking.controller.reportes;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.service.reportes.ReservasReportService;

@RestController
@RequestMapping("/reportes/reservas")
public class ReportesReservasController {

    private final ReservasReportService reservasReportService;

    public ReportesReservasController(ReservasReportService reservasReportService) {
        this.reservasReportService = reservasReportService;
    }

    @GetMapping("/por-estado")
    public ResponseEntity<ReporteResumenKpiResponseDTO> reservasPorEstado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reservasReportService.obtenerReservasPorEstado(fechaDesde, fechaHasta));
    }

    @GetMapping("/proximas")
    public ResponseEntity<ReporteTablaResponseDTO> reservasProximas(
            @RequestParam(required = false) Integer proximosMinutos) {
        return ResponseEntity.ok(reservasReportService.obtenerReservasProximas(proximosMinutos));
    }

    @GetMapping("/cancelaciones/detalle")
    public ResponseEntity<ReporteTablaResponseDTO> cancelacionesDetalle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reservasReportService.obtenerCancelacionesDetalle(fechaDesde, fechaHasta, page, size));
    }

}
