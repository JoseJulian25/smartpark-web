package com.parking.controller.reportes;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteFinancieroResponseDTO;
import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReporteTopNResponseDTO;
import com.parking.service.reportes.FinancierosReportService;

@RestController
@RequestMapping("/reportes/financieros")
public class ReportesFinancierosController {

    private final FinancierosReportService financierosReportService;

    public ReportesFinancierosController(FinancierosReportService financierosReportService) {
        this.financierosReportService = financierosReportService;
    }

    @GetMapping("/ingresos-por-periodo")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> ingresosPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad) {
        return ResponseEntity.ok(financierosReportService.obtenerIngresosPorPeriodo(fechaDesde, fechaHasta, granularidad));
    }

    @GetMapping("/promedios")
    public ResponseEntity<ReporteFinancieroResponseDTO> promediosFinancieros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(financierosReportService.obtenerPromediosFinancieros(fechaDesde, fechaHasta));
    }

    @GetMapping("/ingresos-por-tipo-vehiculo")
    public ResponseEntity<ReporteTablaResponseDTO> ingresosPorTipoVehiculo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(financierosReportService.obtenerIngresosPorTipoVehiculo(fechaDesde, fechaHasta));
    }

    @GetMapping("/ingresos-por-metodo-pago")
    public ResponseEntity<ReporteTablaResponseDTO> ingresosPorMetodoPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(financierosReportService.obtenerIngresosPorMetodoPago(fechaDesde, fechaHasta));
    }

    @GetMapping("/ranking-horas-pico")
    public ResponseEntity<ReporteTopNResponseDTO> rankingHorasPicoPorIngreso(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(financierosReportService.obtenerRankingHorasPicoPorIngreso(fechaDesde, fechaHasta, limite));
    }
}
