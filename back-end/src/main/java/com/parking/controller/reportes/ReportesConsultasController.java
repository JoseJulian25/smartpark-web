package com.parking.controller.reportes;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.service.reportes.ConsultasReportService;

@RestController
@RequestMapping("/reportes/consultas")
public class ReportesConsultasController {

    private final ConsultasReportService consultasReportService;

    public ReportesConsultasController(ConsultasReportService consultasReportService) {
        this.consultasReportService = consultasReportService;
    }

    @GetMapping("/placa")
    public ResponseEntity<ReporteConsultaPlacaResponseDTO> consultaPorPlaca(
            @RequestParam String placa) {
        return ResponseEntity.ok(consultasReportService.obtenerConsultaPorPlaca(placa));
    }

    @GetMapping("/ticket/{codigoTicket}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPorCodigoTicket(
            @PathVariable String codigoTicket) {
        return ResponseEntity.ok(consultasReportService.obtenerConsultaPorCodigoTicket(codigoTicket));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ReporteTablaResponseDTO> listadoTicketsPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(consultasReportService.obtenerListadoTicketsPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/reserva/{codigoReserva}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPorCodigoReserva(
            @PathVariable String codigoReserva) {
        return ResponseEntity.ok(consultasReportService.obtenerConsultaPorCodigoReserva(codigoReserva));
    }

    @GetMapping("/reservas")
    public ResponseEntity<ReporteTablaResponseDTO> listadoReservasPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(consultasReportService.obtenerListadoReservasPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/pago/{codigoTicket}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPagoPorCodigoTicket(
            @PathVariable String codigoTicket) {
        return ResponseEntity.ok(consultasReportService.obtenerConsultaPagoPorCodigoTicket(codigoTicket));
    }

    @GetMapping("/pagos")
    public ResponseEntity<ReporteTablaResponseDTO> listadoPagosPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(consultasReportService.obtenerListadoPagosPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<ReporteTablaResponseDTO> listadoVehiculosPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(consultasReportService.obtenerListadoVehiculosPorFecha(fechaDesde, fechaHasta, page, size));
    }

}
