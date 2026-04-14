package com.parking.service.reportes;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReporteFinancieroResponseDTO;
import com.parking.dto.ReporteIndicadorFinancieroDTO;
import com.parking.dto.ReporteSerieTemporalItemDTO;
import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReporteTopNItemDTO;
import com.parking.dto.ReporteTopNResponseDTO;
import com.parking.entity.Pago;
import com.parking.entity.Ticket;
import com.parking.repository.PagoRepository;

@Service
public class FinancierosReportService {

    private static final String MONEDA_DEFAULT = "GTQ";
    private static final int MAX_RANGE_DIAS = 92;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PagoRepository pagoRepository;

    public FinancierosReportService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public ReporteSerieTemporalResponseDTO obtenerIngresosPorPeriodo(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String granularidad) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        String granularidadNormalizada = normalizarGranularidad(granularidad);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        java.util.Map<String, BigDecimal> ingresosPorPeriodo = pagos.stream()
                .filter(pago -> pago.getHoraPago() != null)
                .collect(Collectors.groupingBy(
                        pago -> construirEtiquetaPeriodo(pago.getHoraPago(), granularidadNormalizada),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                                BigDecimal::add)));

        List<ReporteSerieTemporalItemDTO> items = ingresosPorPeriodo.entrySet().stream()
                .map(entry -> new ReporteSerieTemporalItemDTO(entry.getKey(), entry.getValue()))
                .toList();

        return new ReporteSerieTemporalResponseDTO("Ingresos por " + granularidadNormalizada, MONEDA_DEFAULT, items);
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroResponseDTO obtenerPromediosFinancieros(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        BigDecimal sumaMontos = pagos.stream()
                .map(Pago::getMonto)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketPromedio = pagos.isEmpty()
                ? BigDecimal.ZERO
                : sumaMontos.divide(BigDecimal.valueOf(pagos.size()), 2, java.math.RoundingMode.HALF_UP);

        List<Long> estadiasMinutos = pagos.stream()
                .map(Pago::getTicket)
                .filter(java.util.Objects::nonNull)
                .map(ticket -> calcularEstadiaMinutos(ticket, null))
                .filter(minutos -> minutos > 0)
                .toList();

        BigDecimal estadiaPromedioMinutos = estadiasMinutos.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(
                        estadiasMinutos.stream().mapToLong(Long::longValue).average().orElse(0.0d))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

        List<ReporteIndicadorFinancieroDTO> indicadores = List.of(
                new ReporteIndicadorFinancieroDTO(
                        "TICKET_PROMEDIO",
                        "Ticket promedio",
                        ticketPromedio,
                        MONEDA_DEFAULT,
                        null),
                new ReporteIndicadorFinancieroDTO(
                        "ESTADIA_PROMEDIO_MIN",
                        "Estadia promedio",
                        estadiaPromedioMinutos,
                        "min",
                        null));

        return new ReporteFinancieroResponseDTO(
                "Promedios financieros",
                construirTextoPeriodo(rango),
                MONEDA_DEFAULT,
                indicadores);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerIngresosPorTipoVehiculo(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        java.util.Map<String, BigDecimal> ingresosPorTipo = pagos.stream()
                .collect(Collectors.groupingBy(
                        pago -> {
                            if (pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null
                                    || pago.getTicket().getTipoVehiculo().getNombre() == null) {
                                return "SIN_TIPO";
                            }
                            return pago.getTicket().getTipoVehiculo().getNombre();
                        },
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                                BigDecimal::add)));

        List<String> columnas = List.of("tipoVehiculo", "ingresos", "moneda");
        List<ReporteTablaFilaDTO> filas = ingresosPorTipo.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    java.util.Map<String, String> row = new LinkedHashMap<>();
                    row.put("tipoVehiculo", entry.getKey());
                    row.put("ingresos", entry.getValue().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    row.put("moneda", MONEDA_DEFAULT);
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Ingresos por tipo de vehiculo", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerIngresosPorMetodoPago(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        java.util.Map<String, BigDecimal> ingresosPorMetodo = pagos.stream()
                .collect(Collectors.groupingBy(
                        pago -> normalizarMetodoPago(pago.getMetodoPago()),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                                BigDecimal::add)));

        List<String> columnas = List.of("metodoPago", "ingresos", "moneda");
        List<ReporteTablaFilaDTO> filas = ingresosPorMetodo.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    java.util.Map<String, String> row = new LinkedHashMap<>();
                    row.put("metodoPago", entry.getKey());
                    row.put("ingresos", entry.getValue().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    row.put("moneda", MONEDA_DEFAULT);
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Ingresos por metodo de pago", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTopNResponseDTO obtenerRankingHorasPicoPorIngreso(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer limite) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        int limiteNormalizado = limite == null ? 5 : Math.min(Math.max(limite, 1), 24);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        java.util.Map<Integer, BigDecimal> ingresosPorHora = pagos.stream()
                .filter(pago -> pago.getHoraPago() != null)
                .collect(Collectors.groupingBy(
                        pago -> pago.getHoraPago().getHour(),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                                BigDecimal::add)));

        List<ReporteTopNItemDTO> items = ingresosPorHora.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limiteNormalizado)
                .map(entry -> new ReporteTopNItemDTO(
                        0,
                        String.format("%02d", entry.getKey()),
                        String.format("%02d:00 - %02d:59", entry.getKey(), entry.getKey()),
                        entry.getValue().setScale(2, java.math.RoundingMode.HALF_UP),
                        MONEDA_DEFAULT))
                .toList();

        List<ReporteTopNItemDTO> itemsConPosicion = java.util.stream.IntStream.range(0, items.size())
                .mapToObj(index -> {
                    ReporteTopNItemDTO item = items.get(index);
                    return new ReporteTopNItemDTO(
                            index + 1,
                            item.getClave(),
                            item.getDescripcion(),
                            item.getValor(),
                            item.getUnidad());
                })
                .toList();

        return new ReporteTopNResponseDTO(
                "Ranking de horas pico por ingreso",
                "hora",
                MONEDA_DEFAULT,
                limiteNormalizado,
                itemsConPosicion);
    }

    private RangoFechas resolverRango(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango;
        if (fechaDesde != null && fechaHasta != null) {
            if (fechaHasta.isBefore(fechaDesde)) {
                throw new IllegalArgumentException("fechaHasta no puede ser menor que fechaDesde");
            }
            rango = new RangoFechas(fechaDesde, fechaHasta);
            validarRangoMaximo(rango);
            return rango;
        }

        if (fechaDesde != null) {
            rango = new RangoFechas(fechaDesde, fechaDesde.plusDays(1));
            validarRangoMaximo(rango);
            return rango;
        }

        if (fechaHasta != null) {
            rango = new RangoFechas(fechaHasta.minusDays(1), fechaHasta);
            validarRangoMaximo(rango);
            return rango;
        }

        LocalDate today = LocalDate.now();
        rango = new RangoFechas(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        validarRangoMaximo(rango);
        return rango;
    }

    private void validarRangoMaximo(RangoFechas rango) {
        long dias = Duration.between(rango.fechaDesde(), rango.fechaHasta()).toDays();
        if (dias > MAX_RANGE_DIAS) {
            throw new IllegalArgumentException("El rango maximo permitido es de " + MAX_RANGE_DIAS + " dias");
        }
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private List<Pago> obtenerPagosEnRango(RangoFechas rango) {
        return pagoRepository.findAllByHoraPagoGreaterThanEqualAndHoraPagoLessThan(
                rango.fechaDesde(),
                rango.fechaHasta());
    }

    private String normalizarGranularidad(String granularidad) {
        String value = normalizarTexto(granularidad).toLowerCase(Locale.ROOT);
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

    private String construirTextoPeriodo(RangoFechas rango) {
        return formatDateTime(rango.fechaDesde()) + " a " + formatDateTime(rango.fechaHasta());
    }

    private String normalizarMetodoPago(String metodoPago) {
        String value = normalizarTexto(metodoPago).toUpperCase(Locale.ROOT);
        return value.isBlank() ? "SIN_METODO" : value;
    }

    private long calcularEstadiaMinutos(Ticket ticket, LocalDateTime referencia) {
        if (ticket == null || ticket.getHoraEntrada() == null) {
            return 0L;
        }
        LocalDateTime horaFin = ticket.getHoraSalida();
        if (horaFin == null) {
            horaFin = referencia;
        }
        if (horaFin == null) {
            horaFin = LocalDateTime.of(ticket.getHoraEntrada().toLocalDate(), LocalTime.MAX);
        }
        long minutos = Duration.between(ticket.getHoraEntrada(), horaFin).toMinutes();
        return Math.max(0L, minutos);
    }

    private String normalizarTexto(String value) {
        return value == null ? "" : value.trim();
    }

    private record RangoFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
    }
}
