package com.parking.controller.reportes;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.service.reportes.OperativosReportService;

@RestController
@RequestMapping("/reportes/operativos")
public class ReportesOperativosController {

    private final OperativosReportService operativosReportService;

    public ReportesOperativosController(OperativosReportService operativosReportService) {
        this.operativosReportService = operativosReportService;
    }

    @GetMapping("/entradas-por-hora")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> entradasPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        return ResponseEntity.ok(operativosReportService.obtenerEntradasPorHora(fechaDesde, fechaHasta, usuarioId, tipoVehiculo));
    }

    @GetMapping("/salidas-por-hora")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> salidasPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        return ResponseEntity.ok(operativosReportService.obtenerSalidasPorHora(fechaDesde, fechaHasta, usuarioId, tipoVehiculo));
    }

    @GetMapping("/tickets-activos")
    public ResponseEntity<ReporteTablaResponseDTO> ticketsActivos(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        return ResponseEntity.ok(operativosReportService.obtenerTicketsActivosActuales(usuarioId, tipoVehiculo));
    }

    @GetMapping("/estadias-largas")
    public ResponseEntity<ReporteTablaResponseDTO> estadiasLargas(
            @RequestParam(required = false) Integer umbralMinutos,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        return ResponseEntity.ok(operativosReportService.obtenerEstadiasLargas(umbralMinutos, usuarioId, tipoVehiculo));
    }
}
