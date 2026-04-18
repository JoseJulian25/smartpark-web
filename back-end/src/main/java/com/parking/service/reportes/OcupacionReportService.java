package com.parking.service.reportes;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.entity.Espacio;
import com.parking.entity.Ticket;
import com.parking.repository.EspacioRepository;
import com.parking.repository.TicketRepository;
import com.parking.service.reportes.common.ReportesCommonService;
import com.parking.service.reportes.common.ReportesCommonService.RangoFechas;

@Service
public class OcupacionReportService {

        private static final String ESTADO_TICKET_ANULADO = "ANULADO";
    private static final int MAX_RANGE_DIAS = 92;

    private final EspacioRepository espacioRepository;
    private final TicketRepository ticketRepository;
    private final ReportesCommonService commonService;

    public OcupacionReportService(
            EspacioRepository espacioRepository,
            TicketRepository ticketRepository,
            ReportesCommonService commonService) {
        this.espacioRepository = espacioRepository;
        this.ticketRepository = ticketRepository;
        this.commonService = commonService;
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerUtilizacionBasicaPorEspacio(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);
        List<Espacio> espacios = espacioRepository.findAll();
        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .filter(ticket -> !esTicketAnulado(ticket))
                .toList();

        Map<Long, Long> usoPorEspacioId = tickets.stream()
                .filter(ticket -> ticket.getEspacio() != null && ticket.getEspacio().getId() != null)
                .collect(Collectors.groupingBy(ticket -> ticket.getEspacio().getId(), Collectors.counting()));

        List<String> columnas = List.of("codigoEspacio", "tipoVehiculo", "estadoActual", "activo", "usosEnRango");
        List<ReporteTablaFilaDTO> filas = espacios.stream()
                .sorted(Comparator
                        .comparing((Espacio espacio) -> usoPorEspacioId.getOrDefault(espacio.getId(), 0L), Comparator.reverseOrder())
                        .thenComparing(Espacio::getCodigoEspacio, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(espacio -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("codigoEspacio", espacio.getCodigoEspacio());
                    row.put("tipoVehiculo", espacio.getTipoVehiculo() == null ? "-" : espacio.getTipoVehiculo().getNombre());
                    row.put("estadoActual", espacio.getEstado() == null ? "-" : espacio.getEstado().getNombre());
                    row.put("activo", Boolean.TRUE.equals(espacio.getActivo()) ? "SI" : "NO");
                    row.put("usosEnRango", String.valueOf(usoPorEspacioId.getOrDefault(espacio.getId(), 0L)));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Utilizacion basica por espacio", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerTendenciaUsoPorEspacio(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String granularidad,
            Integer limiteEspacios) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);
        String granularidadNormalizada = normalizarGranularidad(granularidad);
        int limite = limiteEspacios == null ? 8 : Math.min(Math.max(limiteEspacios, 1), 12);

        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .filter(ticket -> !esTicketAnulado(ticket))
                .filter(ticket -> ticket.getHoraEntrada() != null)
                .filter(ticket -> ticket.getEspacio() != null && ticket.getEspacio().getCodigoEspacio() != null)
                .toList();

        Map<String, Long> totalPorEspacio = tickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getEspacio().getCodigoEspacio(),
                        Collectors.counting()));

        List<String> espaciosTop = totalPorEspacio.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                .limit(limite)
                .map(Map.Entry::getKey)
                .toList();

        Set<String> espaciosSet = Set.copyOf(espaciosTop);

        Map<String, Map<String, Long>> usosPorPeriodoYEspacio = tickets.stream()
                .filter(ticket -> espaciosSet.contains(ticket.getEspacio().getCodigoEspacio()))
                .collect(Collectors.groupingBy(
                        ticket -> construirEtiquetaPeriodo(ticket.getHoraEntrada(), granularidadNormalizada),
                        Collectors.groupingBy(
                                ticket -> ticket.getEspacio().getCodigoEspacio(),
                                Collectors.counting())));

        List<String> periodos = usosPorPeriodoYEspacio.keySet().stream()
                .sorted()
                .toList();

        List<String> columnas = List.of("periodo", "codigoEspacio", "usos");
        List<ReporteTablaFilaDTO> filas = periodos.stream()
                .flatMap(periodo -> espaciosTop.stream().map(codigoEspacio -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("periodo", periodo);
                    row.put("codigoEspacio", codigoEspacio);
                    row.put("usos", String.valueOf(
                            usosPorPeriodoYEspacio
                                    .getOrDefault(periodo, java.util.Collections.emptyMap())
                                    .getOrDefault(codigoEspacio, 0L)));
                    return new ReporteTablaFilaDTO(row);
                }))
                .toList();

        return new ReporteTablaResponseDTO(
                "Tendencia de uso por espacio",
                columnas,
                filas,
                (long) filas.size());
    }

    private String normalizarGranularidad(String granularidad) {
        String value = commonService.normalizarTexto(granularidad).toLowerCase(Locale.ROOT);
        if (value.isBlank()) {
            return "dia";
        }
        if (value.equals("hora")) {
            return "dia";
        }
        if (value.equals("dia") || value.equals("semana") || value.equals("mes")) {
            return value;
        }
        throw new IllegalArgumentException("granularidad invalida. Use: dia, semana o mes");
    }

    private String construirEtiquetaPeriodo(LocalDateTime fechaHora, String granularidad) {
        return switch (granularidad) {
            case "mes" -> String.format("%04d-%02d", fechaHora.getYear(), fechaHora.getMonthValue());
            case "semana" -> {
                WeekFields wf = WeekFields.ISO;
                int week = fechaHora.get(wf.weekOfWeekBasedYear());
                int year = fechaHora.get(wf.weekBasedYear());
                yield String.format("%04d-W%02d", year, week);
            }
            default -> fechaHora.toLocalDate().toString();
        };
    }

        private boolean esTicketAnulado(Ticket ticket) {
                return ticket.getEstado() != null
                                && ticket.getEstado().getNombre() != null
                                && ESTADO_TICKET_ANULADO.equalsIgnoreCase(ticket.getEstado().getNombre());
        }
}
