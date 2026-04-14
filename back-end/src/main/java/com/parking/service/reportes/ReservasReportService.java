package com.parking.service.reportes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReporteKpiDTO;
import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.entity.Reserva;
import com.parking.repository.ReservaRepository;
import com.parking.service.reportes.common.ReportesCommonService;
import com.parking.service.reportes.common.ReportesCommonService.RangoFechas;

@Service
public class ReservasReportService {

    private static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_RESERVA_ACTIVA = "ACTIVA";
    private static final String ESTADO_RESERVA_FINALIZADA = "FINALIZADA";
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    private static final int MAX_RANGE_DIAS = 92;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 200;

    private final ReservaRepository reservaRepository;
    private final ReportesCommonService commonService;

    public ReservasReportService(
            ReservaRepository reservaRepository,
            ReportesCommonService commonService) {
        this.reservaRepository = reservaRepository;
        this.commonService = commonService;
    }

    @Transactional(readOnly = true)
    public ReporteResumenKpiResponseDTO obtenerReservasPorEstado(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);
        List<Reserva> reservas = obtenerReservasCreadasEnRango(rango);

        long pendientes = contarPorEstado(reservas, ESTADO_RESERVA_PENDIENTE);
        long activas = contarPorEstado(reservas, ESTADO_RESERVA_ACTIVA);
        long finalizadas = contarPorEstado(reservas, ESTADO_RESERVA_FINALIZADA);
        long canceladas = contarPorEstado(reservas, ESTADO_RESERVA_CANCELADA);

        List<ReporteKpiDTO> kpis = List.of(
                new ReporteKpiDTO("RES_PENDIENTES", "Pendientes", BigDecimal.valueOf(pendientes), "reservas"),
                new ReporteKpiDTO("RES_ACTIVAS", "Activas", BigDecimal.valueOf(activas), "reservas"),
                new ReporteKpiDTO("RES_FINALIZADAS", "Finalizadas", BigDecimal.valueOf(finalizadas), "reservas"),
                new ReporteKpiDTO("RES_CANCELADAS", "Canceladas", BigDecimal.valueOf(canceladas), "reservas"));

        return new ReporteResumenKpiResponseDTO("Reservas por estado", kpis);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerReservasProximas(Integer proximosMinutos) {
        int minutos = proximosMinutos == null || proximosMinutos < 1 ? 30 : proximosMinutos;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limite = now.plusMinutes(minutos);

        List<Reserva> reservasProximas = reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(reserva -> reserva.getEstado() != null)
                .filter(reserva -> ESTADO_RESERVA_PENDIENTE.equalsIgnoreCase(reserva.getEstado().getNombre()))
                .filter(reserva -> reserva.getHoraInicio() != null)
                .filter(reserva -> !reserva.getHoraInicio().isBefore(now) && !reserva.getHoraInicio().isAfter(limite))
                .sorted((a, b) -> a.getHoraInicio().compareTo(b.getHoraInicio()))
                .toList();

        List<String> columnas = List.of("codigoReserva", "cliente", "placa", "tipoVehiculo", "codigoEspacio", "horaInicio");
        List<ReporteTablaFilaDTO> filas = reservasProximas.stream()
                .map(reserva -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("codigoReserva", reserva.getCodigoReserva());
                    row.put("cliente", reserva.getClienteNombreCompleto());
                    row.put("placa", reserva.getPlaca());
                    row.put("tipoVehiculo", reserva.getTipoVehiculo().getNombre());
                    row.put("codigoEspacio", reserva.getEspacio().getCodigoEspacio());
                    row.put("horaInicio", commonService.formatDateTime(reserva.getHoraInicio()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Reservas proximas", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerCancelacionesDetalle(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);
        List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

        List<String> columnas = List.of(
                "codigoReserva",
                "cliente",
                "placa",
                "codigoEspacio",
                "horaInicio",
                "horaCancelacion",
                "canceladoPor",
                "motivoCancelacion");

        List<ReporteTablaFilaDTO> filas = canceladas.stream()
                .map(reserva -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("codigoReserva", reserva.getCodigoReserva());
                    row.put("cliente", reserva.getClienteNombreCompleto());
                    row.put("placa", reserva.getPlaca());
                    row.put("codigoEspacio", reserva.getEspacio().getCodigoEspacio());
                    row.put("horaInicio", commonService.formatDateTime(reserva.getHoraInicio()));
                    row.put("horaCancelacion", commonService.formatDateTime(reserva.getHoraFin()));
                    row.put("canceladoPor", reserva.getCanceladoPor() == null || reserva.getCanceladoPor().getUsername() == null
                            || reserva.getCanceladoPor().getUsername().isBlank() ? "SIN_USUARIO" : reserva.getCanceladoPor().getUsername());
                    row.put("motivoCancelacion", commonService.normalizarMotivo(reserva.getMotivoCancelacion()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO(
                "Cancelaciones de reservas (detalle)",
                columnas,
                commonService.paginarFilas(filas, page, size, DEFAULT_PAGE, DEFAULT_SIZE, MAX_SIZE),
                (long) filas.size());
    }

    private List<Reserva> obtenerReservasCreadasEnRango(RangoFechas rango) {
        return reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(reserva -> reserva.getFechaCreacion() != null)
                .filter(reserva -> !reserva.getFechaCreacion().isBefore(rango.fechaDesde())
                        && reserva.getFechaCreacion().isBefore(rango.fechaHasta()))
                .toList();
    }

    private List<Reserva> obtenerReservasCanceladasEnRango(RangoFechas rango) {
        return reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(reserva -> reserva.getEstado() != null)
                .filter(reserva -> ESTADO_RESERVA_CANCELADA.equalsIgnoreCase(reserva.getEstado().getNombre()))
                .filter(reserva -> reserva.getHoraFin() != null)
                .filter(reserva -> !reserva.getHoraFin().isBefore(rango.fechaDesde())
                        && reserva.getHoraFin().isBefore(rango.fechaHasta()))
                .toList();
    }

    private long contarPorEstado(List<Reserva> reservas, String estadoObjetivo) {
        return reservas.stream()
                .filter(reserva -> reserva.getEstado() != null)
                .filter(reserva -> estadoObjetivo.equalsIgnoreCase(reserva.getEstado().getNombre()))
                .count();
    }
}
