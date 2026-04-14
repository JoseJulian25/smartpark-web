package com.parking.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReporteComparativoResponseDTO;
import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReporteFinancieroResponseDTO;
import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReporteTopNResponseDTO;
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

    @GetMapping("/ocupacion/tendencia-uso-espacio")
    public ResponseEntity<ReporteTablaResponseDTO> tendenciaUsoPorEspacio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String granularidad,
            @RequestParam(required = false) Integer limiteEspacios) {
        return ResponseEntity.ok(reportesService.obtenerTendenciaUsoPorEspacio(fechaDesde, fechaHasta, granularidad, limiteEspacios));
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

    @GetMapping("/consultas/tickets")
    public ResponseEntity<ReporteTablaResponseDTO> listadoTicketsPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerListadoTicketsPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/consultas/reserva/{codigoReserva}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPorCodigoReserva(
            @PathVariable String codigoReserva) {
        return ResponseEntity.ok(reportesService.obtenerConsultaPorCodigoReserva(codigoReserva));
    }

    @GetMapping("/consultas/reservas")
    public ResponseEntity<ReporteTablaResponseDTO> listadoReservasPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerListadoReservasPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/consultas/pago/{codigoTicket}")
    public ResponseEntity<ReporteTablaResponseDTO> consultaPagoPorCodigoTicket(
            @PathVariable String codigoTicket) {
        return ResponseEntity.ok(reportesService.obtenerConsultaPagoPorCodigoTicket(codigoTicket));
    }

    @GetMapping("/consultas/pagos")
    public ResponseEntity<ReporteTablaResponseDTO> listadoPagosPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerListadoPagosPorFecha(fechaDesde, fechaHasta, page, size));
    }

    @GetMapping("/consultas/vehiculos")
    public ResponseEntity<ReporteTablaResponseDTO> listadoVehiculosPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(reportesService.obtenerListadoVehiculosPorFecha(fechaDesde, fechaHasta, page, size));
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

}
