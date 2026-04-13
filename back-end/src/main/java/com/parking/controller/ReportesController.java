package com.parking.controller;

import java.util.Map;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReporteComparativoResponseDTO;
import com.parking.dto.ReporteFinancieroResponseDTO;
import com.parking.dto.ReporteTopNResponseDTO;
import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReportesBootstrapResponseDTO;
import com.parking.service.ReportesService;

@RestController
@RequestMapping("/reportes")
public class ReportesController {

    private final ReportesService reportesService;

    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    @GetMapping("/bootstrap")
    public ResponseEntity<ReportesBootstrapResponseDTO> obtenerBootstrap() {
        return ResponseEntity.ok(reportesService.obtenerBootstrap());
    }

    @GetMapping("/operativos")
    public ResponseEntity<Map<String, String>> estadoOperativos() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Operativos", "/reportes/operativos"));
    }

    @GetMapping("/operativos/entradas-por-hora")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> entradasPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reportesService.obtenerEntradasPorHora(fechaDesde, fechaHasta, usuarioId));
    }

    @GetMapping("/operativos/salidas-por-hora")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> salidasPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reportesService.obtenerSalidasPorHora(fechaDesde, fechaHasta, usuarioId));
    }

    @GetMapping("/operativos/flujo-neto-por-hora")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> flujoNetoPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reportesService.obtenerFlujoNetoPorHora(fechaDesde, fechaHasta, usuarioId));
    }

    @GetMapping("/operativos/tickets-activos")
    public ResponseEntity<ReporteTablaResponseDTO> ticketsActivos(
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reportesService.obtenerTicketsActivosActuales(usuarioId));
    }

    @GetMapping("/operativos/estadias-largas")
    public ResponseEntity<ReporteTablaResponseDTO> estadiasLargas(
            @RequestParam(required = false) Integer umbralMinutos,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reportesService.obtenerEstadiasLargas(umbralMinutos, usuarioId));
    }

    @GetMapping("/reservas")
    public ResponseEntity<Map<String, String>> estadoReservas() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Reservas", "/reportes/reservas"));
    }

    @GetMapping("/reservas/por-estado")
    public ResponseEntity<ReporteResumenKpiResponseDTO> reservasPorEstado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerReservasPorEstado(fechaDesde, fechaHasta));
    }

    @GetMapping("/reservas/creadas-por-dia")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> reservasCreadasPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerReservasCreadasPorDia(fechaDesde, fechaHasta));
    }

    @GetMapping("/reservas/proximas")
    public ResponseEntity<ReporteTablaResponseDTO> reservasProximas(
            @RequestParam(required = false) Integer proximosMinutos) {
        return ResponseEntity.ok(reportesService.obtenerReservasProximas(proximosMinutos));
    }

    @GetMapping("/reservas/cancelaciones/detalle")
    public ResponseEntity<ReporteTablaResponseDTO> cancelacionesDetalle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerCancelacionesDetalle(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/reservas/cancelaciones/conteo-por-motivo")
    public ResponseEntity<ReporteTablaResponseDTO> cancelacionesConteoPorMotivo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerCancelacionesConteoPorMotivo(fechaDesde, fechaHasta));
    }

    @GetMapping("/ocupacion")
    public ResponseEntity<Map<String, String>> estadoOcupacion() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Ocupacion", "/reportes/ocupacion"));
    }

    @GetMapping("/ocupacion/global")
    public ResponseEntity<ReporteResumenKpiResponseDTO> ocupacionActualGlobal() {
        return ResponseEntity.ok(reportesService.obtenerOcupacionActualGlobal());
    }

    @GetMapping("/ocupacion/por-tipo")
    public ResponseEntity<ReporteTablaResponseDTO> ocupacionPorTipoVehiculo() {
        return ResponseEntity.ok(reportesService.obtenerOcupacionPorTipoVehiculo());
    }

    @GetMapping("/ocupacion/capacidad")
    public ResponseEntity<ReporteResumenKpiResponseDTO> capacidadActivaInactiva() {
        return ResponseEntity.ok(reportesService.obtenerCapacidadActivaInactiva());
    }

    @GetMapping("/ocupacion/utilizacion-por-espacio")
    public ResponseEntity<ReporteTablaResponseDTO> utilizacionBasicaPorEspacio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerUtilizacionBasicaPorEspacio(fechaDesde, fechaHasta));
    }

    @GetMapping("/consultas")
    public ResponseEntity<Map<String, String>> estadoConsultas() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Consultas", "/reportes/consultas"));
    }

    @GetMapping("/consultas/placa")
    public ResponseEntity<ReporteConsultaPlacaResponseDTO> consultaPorPlaca(
            @RequestParam String placa) {
        return ResponseEntity.ok(reportesService.obtenerConsultaPorPlaca(placa));
    }

    @GetMapping("/consultas/ticket/{codigoTicket}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPorCodigoTicket(
            @PathVariable String codigoTicket) {
        return ResponseEntity.ok(reportesService.obtenerConsultaPorCodigoTicket(codigoTicket));
    }

    @GetMapping("/consultas/reserva/{codigoReserva}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPorCodigoReserva(
            @PathVariable String codigoReserva) {
        return ResponseEntity.ok(reportesService.obtenerConsultaPorCodigoReserva(codigoReserva));
    }

    @GetMapping("/consultas/historial-cliente")
    public ResponseEntity<ReporteTablaResponseDTO> historialConsolidadoCliente(
            @RequestParam String placa,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerHistorialConsolidadoCliente(placa, page, size));
    }

    @GetMapping("/consultas/trazabilidad-ticket/{codigoTicket}")
    public ResponseEntity<ReporteTablaResponseDTO> trazabilidadTicket(
            @PathVariable String codigoTicket) {
        return ResponseEntity.ok(reportesService.obtenerTrazabilidadTicket(codigoTicket));
    }

    @GetMapping("/consultas/rango-montos")
    public ResponseEntity<ReporteTablaResponseDTO> consultasPorRangoMontos(
            @RequestParam(required = false) BigDecimal montoDesde,
            @RequestParam(required = false) BigDecimal montoHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerConsultasPorRangoMontos(montoDesde, montoHasta, fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/financieros")
    public ResponseEntity<Map<String, String>> estadoFinancieros() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Financieros", "/reportes/financieros"));
    }

    @GetMapping("/financieros/ingresos-por-periodo")
    public ResponseEntity<ReporteSerieTemporalResponseDTO> ingresosPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad) {
        return ResponseEntity.ok(reportesService.obtenerIngresosPorPeriodo(fechaDesde, fechaHasta, granularidad));
    }

    @GetMapping("/financieros/promedios")
    public ResponseEntity<ReporteFinancieroResponseDTO> promediosFinancieros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerPromediosFinancieros(fechaDesde, fechaHasta));
    }

    @GetMapping("/financieros/ingresos-por-tipo-vehiculo")
    public ResponseEntity<ReporteTablaResponseDTO> ingresosPorTipoVehiculo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerIngresosPorTipoVehiculo(fechaDesde, fechaHasta));
    }

    @GetMapping("/financieros/ingresos-por-metodo-pago")
    public ResponseEntity<ReporteTablaResponseDTO> ingresosPorMetodoPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerIngresosPorMetodoPago(fechaDesde, fechaHasta));
    }

    @GetMapping("/financieros/ranking-horas-pico")
    public ResponseEntity<ReporteTopNResponseDTO> rankingHorasPicoPorIngreso(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(reportesService.obtenerRankingHorasPicoPorIngreso(fechaDesde, fechaHasta, limite));
    }

    @GetMapping("/comparativos")
    public ResponseEntity<Map<String, String>> estadoComparativos() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Comparativos", "/reportes/comparativos"));
    }

    @GetMapping("/comparativos/entradas-salidas")
    public ResponseEntity<ReporteComparativoResponseDTO> comparativoEntradasSalidas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String modoComparacion) {
        return ResponseEntity.ok(reportesService.obtenerComparativoEntradasSalidas(fechaDesde, fechaHasta, modoComparacion));
    }

    @GetMapping("/comparativos/reservas-por-estado")
    public ResponseEntity<ReporteComparativoResponseDTO> comparativoReservasPorEstado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String modoComparacion) {
        return ResponseEntity.ok(reportesService.obtenerComparativoReservasPorEstado(fechaDesde, fechaHasta, modoComparacion));
    }

    @GetMapping("/comparativos/ocupacion-franja-horaria")
    public ResponseEntity<ReporteComparativoResponseDTO> comparativoOcupacionPorFranja(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String modoComparacion) {
        return ResponseEntity.ok(reportesService.obtenerComparativoOcupacionPorFranja(fechaDesde, fechaHasta, modoComparacion));
    }

    @GetMapping("/eficiencia")
    public ResponseEntity<Map<String, String>> estadoEficiencia() {
        return ResponseEntity.ok(reportesService.obtenerEstadoSeccion("Eficiencia", "/reportes/eficiencia"));
    }

    @GetMapping("/eficiencia/tasa-conversion-reserva-ingreso")
    public ResponseEntity<ReporteResumenKpiResponseDTO> tasaConversionReservaIngreso(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerTasaConversionReservaIngreso(fechaDesde, fechaHasta));
    }

    @GetMapping("/eficiencia/tasa-no-show")
    public ResponseEntity<ReporteResumenKpiResponseDTO> tasaNoShow(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerTasaNoShowReservas(fechaDesde, fechaHasta));
    }

    @GetMapping("/eficiencia/cancelaciones-por-operador")
    public ResponseEntity<ReporteTablaResponseDTO> cancelacionesPorOperador(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerCancelacionesPorOperador(fechaDesde, fechaHasta));
    }

    @GetMapping("/eficiencia/tiempo-promedio-ocupacion-por-tipo")
    public ResponseEntity<ReporteTablaResponseDTO> tiempoPromedioOcupacionPorTipo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return ResponseEntity.ok(reportesService.obtenerTiempoPromedioOcupacionPorTipo(fechaDesde, fechaHasta));
    }

    @GetMapping("/export/csv/tickets")
    public ResponseEntity<ByteArrayResource> exportarTicketsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        String fileName = reportesService.construirNombreArchivoCsv("tickets");
        byte[] data = reportesService.exportarTicketsEnRangoCsv(fechaDesde, fechaHasta);
        return construirRespuestaCsv(fileName, data);
    }

    @GetMapping("/export/csv/reservas")
    public ResponseEntity<ByteArrayResource> exportarReservasCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        String fileName = reportesService.construirNombreArchivoCsv("reservas");
        byte[] data = reportesService.exportarReservasEnRangoCsv(fechaDesde, fechaHasta);
        return construirRespuestaCsv(fileName, data);
    }

    @GetMapping("/export/csv/cancelaciones")
    public ResponseEntity<ByteArrayResource> exportarCancelacionesCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        String fileName = reportesService.construirNombreArchivoCsv("cancelaciones_reservas");
        byte[] data = reportesService.exportarCancelacionesConMotivoCsv(fechaDesde, fechaHasta);
        return construirRespuestaCsv(fileName, data);
    }

    @GetMapping("/export/pdf/resumen-operativo-diario")
    public ResponseEntity<ByteArrayResource> exportarResumenOperativoDiarioPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad) {
        String fileName = reportesService.construirNombreArchivoPdf("resumen_operativo");
        byte[] data = reportesService.exportarResumenOperativoDiarioPdf(fechaDesde, fechaHasta, granularidad);
        return construirRespuestaPdf(fileName, data);
    }

    @GetMapping("/export/pdf/cancelaciones")
    public ResponseEntity<ByteArrayResource> exportarCancelacionesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad) {
        String fileName = reportesService.construirNombreArchivoPdf("cancelaciones_reservas");
        byte[] data = reportesService.exportarCancelacionesConMotivoPdf(fechaDesde, fechaHasta, granularidad);
        return construirRespuestaPdf(fileName, data);
    }

    @GetMapping("/export/csv/operativos-avanzado")
    public ResponseEntity<ByteArrayResource> exportarOperativosAvanzadoCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        String fileName = reportesService.construirNombreArchivoEstandar("reportes", "operativos_avanzado", "csv");
        byte[] data = reportesService.exportarOperativosAvanzadoCsv(fechaDesde, fechaHasta, usuarioId, tipoVehiculo);
        return construirRespuestaCsv(fileName, data);
    }

    @GetMapping("/export/csv/financieros-avanzado")
    public ResponseEntity<ByteArrayResource> exportarFinancierosAvanzadoCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo) {
        String fileName = reportesService.construirNombreArchivoEstandar("reportes", "financieros_avanzado", "csv");
        byte[] data = reportesService.exportarFinancierosAvanzadoCsv(fechaDesde, fechaHasta, usuarioId, tipoVehiculo);
        return construirRespuestaCsv(fileName, data);
    }

    @GetMapping("/export/pdf/resumen-ejecutivo")
    public ResponseEntity<ByteArrayResource> exportarResumenEjecutivoPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tipoVehiculo,
            @RequestParam(required = false) String granularidad) {
        String fileName = reportesService.construirNombreArchivoEstandar("reportes", "resumen_ejecutivo", "pdf");
        byte[] data = reportesService.exportarResumenEjecutivoPdf(fechaDesde, fechaHasta, usuarioId, tipoVehiculo, granularidad);
        return construirRespuestaPdf(fileName, data);
    }

    private ResponseEntity<ByteArrayResource> construirRespuestaCsv(String fileName, byte[] data) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    private ResponseEntity<ByteArrayResource> construirRespuestaPdf(String fileName, byte[] data) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }
}
