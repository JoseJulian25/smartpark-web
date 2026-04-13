package com.parking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.awt.Color;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReporteComparativoItemDTO;
import com.parking.dto.ReporteComparativoResponseDTO;
import com.parking.dto.ReporteFinancieroResponseDTO;
import com.parking.dto.ReporteIndicadorFinancieroDTO;
import com.parking.dto.ReporteKpiDTO;
import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteSerieTemporalItemDTO;
import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteSeccionDTO;
import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReporteTopNItemDTO;
import com.parking.dto.ReporteTopNResponseDTO;
import com.parking.dto.ReportesBootstrapResponseDTO;
import com.parking.entity.Espacio;
import com.parking.entity.Pago;
import com.parking.entity.Reserva;
import com.parking.entity.Ticket;
import com.parking.repository.EspacioRepository;
import com.parking.repository.PagoRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.TicketRepository;

@Service
public class ReportesService {

    private static final String ESTADO_TICKET_ACTIVO = "ACTIVO";
    private static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_RESERVA_ACTIVA = "ACTIVA";
    private static final String ESTADO_RESERVA_FINALIZADA = "FINALIZADA";
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    private static final String MODO_COMPARACION_PERIODO_ANTERIOR = "periodoanterior";
    private static final String MODO_COMPARACION_MISMO_PERIODO_ANIO_ANTERIOR = "mismoperiodoanioanterior";
    private static final Set<String> ESTADOS_OCUPADOS_ESPACIO = Set.of("OCUPADO", "RESERVADO");
    private static final String MONEDA_DEFAULT = "GTQ";
    private static final int MAX_RANGE_DIAS = 92;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 200;
    private static final Color PDF_PRIMARY_COLOR = new Color(15, 23, 42);
    private static final Color PDF_SECONDARY_TEXT = new Color(71, 85, 105);
    private static final Color PDF_HEADER_BG = new Color(226, 232, 240);
    private static final Color PDF_BAR_COLOR = new Color(14, 116, 144);
    private static final Color PDF_BAR_BG = new Color(226, 232, 240);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FILE_STANDARD_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final TicketRepository ticketRepository;
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final PagoRepository pagoRepository;

    public ReportesService(
            TicketRepository ticketRepository,
            ReservaRepository reservaRepository,
            EspacioRepository espacioRepository,
            PagoRepository pagoRepository) {
        this.ticketRepository = ticketRepository;
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable("reportesBootstrap")
    public ReportesBootstrapResponseDTO obtenerBootstrap() {
        ReporteSeccionDTO operativos = new ReporteSeccionDTO();
        operativos.setNombre("Operativos");
        operativos.setRutaBase("/reportes/operativos");
        operativos.setDescripcion("Reportes de entradas, salidas y tickets activos");

        ReporteSeccionDTO reservas = new ReporteSeccionDTO();
        reservas.setNombre("Reservas");
        reservas.setRutaBase("/reportes/reservas");
        reservas.setDescripcion("Reportes por estado, cancelaciones y proyecciones");

        ReporteSeccionDTO ocupacion = new ReporteSeccionDTO();
        ocupacion.setNombre("Ocupacion");
        ocupacion.setRutaBase("/reportes/ocupacion");
        ocupacion.setDescripcion("Reportes de capacidad, ocupacion y uso por espacio");

        ReporteSeccionDTO consultas = new ReporteSeccionDTO();
        consultas.setNombre("Consultas");
        consultas.setRutaBase("/reportes/consultas");
        consultas.setDescripcion("Consultas especificas por placa, ticket y reserva");

        return new ReportesBootstrapResponseDTO(
                "Reportes y Consultas",
                List.of(
                operativos,
                reservas,
                ocupacion,
                consultas));
    }

    @Transactional(readOnly = true)
    public Map<String, String> obtenerEstadoSeccion(String seccion, String rutaBase) {
        return Map.of(
                "seccion", seccion,
                "rutaBase", rutaBase,
                "estado", "LISTA_PARA_IMPLEMENTACION",
                "mensaje", "Estructura base creada. Pendiente implementacion de endpoints funcionales en tareas siguientes.");
    }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerEntradasPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta, Long usuarioId) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = filtrarPorUsuario(ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta()), usuarioId);

        return construirSeriePorHora(
            tickets,
            Ticket::getHoraEntrada,
            "Entradas por hora",
            "tickets");
        }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerSalidasPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta, Long usuarioId) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = filtrarPorUsuario(ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta()), usuarioId);

        return construirSeriePorHora(
            tickets,
            Ticket::getHoraSalida,
            "Salidas por hora",
            "tickets");
        }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerFlujoNetoPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta, Long usuarioId) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> entradas = filtrarPorUsuario(ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta()), usuarioId);
        List<Ticket> salidas = filtrarPorUsuario(ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta()), usuarioId);

        long[] entradasPorHora = agruparPorHora(entradas, Ticket::getHoraEntrada);
        long[] salidasPorHora = agruparPorHora(salidas, Ticket::getHoraSalida);

        List<ReporteSerieTemporalItemDTO> items = java.util.stream.IntStream.range(0, 24)
            .mapToObj(hora -> new ReporteSerieTemporalItemDTO(
                String.format("%02d:00", hora),
                BigDecimal.valueOf(entradasPorHora[hora] - salidasPorHora[hora])))
            .toList();

        return new ReporteSerieTemporalResponseDTO("Flujo neto por hora", "tickets", items);
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerTicketsActivosActuales(Long usuarioId) {
            List<Ticket> activos = filtrarPorUsuario(ticketRepository.findAll(), usuarioId).stream()
                    .filter(ticket -> ticket.getEstado() != null)
                    .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                    .sorted((a, b) -> {
                        LocalDateTime first = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                        LocalDateTime second = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                        return second.compareTo(first);
                    })
                    .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "tipoVehiculo", "codigoEspacio", "usuario", "horaEntrada", "estado");
        List<ReporteTablaFilaDTO> filas = activos.stream()
            .map(ticket -> {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("codigoTicket", ticket.getCodigoTicket());
                row.put("placa", ticket.getPlaca());
                row.put("tipoVehiculo", ticket.getTipoVehiculo().getNombre());
                row.put("codigoEspacio", ticket.getEspacio().getCodigoEspacio());
            row.put("usuario", obtenerEtiquetaUsuario(ticket));
                row.put("horaEntrada", formatDateTime(ticket.getHoraEntrada()));
                row.put("estado", ticket.getEstado().getNombre());
                return new ReporteTablaFilaDTO(row);
            })
            .toList();

        return new ReporteTablaResponseDTO(
            "Tickets activos actuales",
            columnas,
            filas,
            (long) filas.size());
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerEstadiasLargas(Integer umbralMinutos, Long usuarioId) {
        int umbral = umbralMinutos == null || umbralMinutos < 1 ? 360 : umbralMinutos;
        LocalDateTime now = LocalDateTime.now();

            List<Ticket> activos = filtrarPorUsuario(ticketRepository.findAll(), usuarioId).stream()
                    .filter(ticket -> ticket.getEstado() != null)
                    .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                    .sorted((a, b) -> {
                        LocalDateTime first = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                        LocalDateTime second = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                        return second.compareTo(first);
                    })
                    .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "codigoEspacio", "usuario", "horaEntrada", "minutosEstadia");
        List<ReporteTablaFilaDTO> filas = activos.stream()
            .filter(ticket -> ticket.getHoraEntrada() != null)
            .map(ticket -> {
                long minutos = Duration.between(ticket.getHoraEntrada(), now).toMinutes();
                Map<String, String> row = new LinkedHashMap<>();
                row.put("codigoTicket", ticket.getCodigoTicket());
                row.put("placa", ticket.getPlaca());
                row.put("codigoEspacio", ticket.getEspacio().getCodigoEspacio());
            row.put("usuario", obtenerEtiquetaUsuario(ticket));
                row.put("horaEntrada", formatDateTime(ticket.getHoraEntrada()));
                row.put("minutosEstadia", String.valueOf(Math.max(0, minutos)));
                return new ReporteTablaFilaDTO(row);
            })
            .filter(row -> Long.parseLong(row.getColumnas().getOrDefault("minutosEstadia", "0")) >= umbral)
            .toList();

        return new ReporteTablaResponseDTO(
            "Estadias largas",
            columnas,
            filas,
            (long) filas.size());
        }

    @Transactional(readOnly = true)
    public ReporteResumenKpiResponseDTO obtenerReservasPorEstado(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
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
    public ReporteSerieTemporalResponseDTO obtenerReservasCreadasPorDia(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> reservas = obtenerReservasCreadasEnRango(rango);

        Map<LocalDate, Long> conteoPorDia = reservas.stream()
                .filter(reserva -> reserva.getFechaCreacion() != null)
                .collect(Collectors.groupingBy(
                        reserva -> reserva.getFechaCreacion().toLocalDate(),
                        Collectors.counting()));

        LocalDate startDate = rango.fechaDesde().toLocalDate();
        LocalDate endDate = rango.fechaHasta().toLocalDate();
        if (rango.fechaHasta().toLocalTime().equals(java.time.LocalTime.MIDNIGHT) && endDate.isAfter(startDate)) {
            endDate = endDate.minusDays(1);
        }

        List<ReporteSerieTemporalItemDTO> items = new java.util.ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            items.add(new ReporteSerieTemporalItemDTO(
                    cursor.toString(),
                    BigDecimal.valueOf(conteoPorDia.getOrDefault(cursor, 0L))));
            cursor = cursor.plusDays(1);
        }

        return new ReporteSerieTemporalResponseDTO("Reservas creadas por dia", "reservas", items);
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
                    row.put("horaInicio", formatDateTime(reserva.getHoraInicio()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Reservas proximas", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerCancelacionesDetalle(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        return obtenerCancelacionesDetalle(fechaDesde, fechaHasta, null, null);
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerCancelacionesDetalle(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
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
                    row.put("horaInicio", formatDateTime(reserva.getHoraInicio()));
                    row.put("horaCancelacion", formatDateTime(reserva.getHoraFin()));
                        row.put("canceladoPor", reserva.getCanceladoPor() == null || reserva.getCanceladoPor().getUsername() == null
                            || reserva.getCanceladoPor().getUsername().isBlank() ? "SIN_USUARIO" : reserva.getCanceladoPor().getUsername());
                    row.put("motivoCancelacion", normalizarMotivo(reserva.getMotivoCancelacion()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return paginarTabla("Cancelaciones de reservas (detalle)", columnas, filas, page, size);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerCancelacionesConteoPorMotivo(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

        Map<String, Long> conteoPorMotivo = canceladas.stream()
                .collect(Collectors.groupingBy(
                        reserva -> normalizarMotivo(reserva.getMotivoCancelacion()),
                        Collectors.counting()));

        List<String> columnas = List.of("motivoCancelacion", "cantidad");
        List<ReporteTablaFilaDTO> filas = conteoPorMotivo.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("motivoCancelacion", entry.getKey());
                    row.put("cantidad", String.valueOf(entry.getValue()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Cancelaciones por motivo", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    @Cacheable("reportesOcupacionGlobal")
    public ReporteResumenKpiResponseDTO obtenerOcupacionActualGlobal() {
    List<Espacio> espaciosActivos = espacioRepository.findAllByActivoTrueOrderByIdAsc();
    long totalActivos = espaciosActivos.size();
    long ocupados = espaciosActivos.stream().filter(this::estaOcupado).count();
    long libres = Math.max(0, totalActivos - ocupados);

    BigDecimal porcentaje = totalActivos == 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf((ocupados * 100.0d) / totalActivos);

    List<ReporteKpiDTO> kpis = List.of(
        new ReporteKpiDTO("OCUP_TOTAL_ACTIVOS", "Espacios activos", BigDecimal.valueOf(totalActivos), "espacios"),
        new ReporteKpiDTO("OCUP_OCUPADOS", "Ocupados", BigDecimal.valueOf(ocupados), "espacios"),
        new ReporteKpiDTO("OCUP_LIBRES", "Libres", BigDecimal.valueOf(libres), "espacios"),
        new ReporteKpiDTO("OCUP_PORCENTAJE", "Ocupacion global", porcentaje, "%"));

    return new ReporteResumenKpiResponseDTO("Ocupacion actual global", kpis);
    }

    @Transactional(readOnly = true)
    @Cacheable("reportesOcupacionTipo")
    public ReporteTablaResponseDTO obtenerOcupacionPorTipoVehiculo() {
    List<Espacio> espaciosActivos = espacioRepository.findAllByActivoTrueOrderByIdAsc();
    Map<String, List<Espacio>> porTipo = espaciosActivos.stream()
        .collect(Collectors.groupingBy(
            espacio -> espacio.getTipoVehiculo() == null ? "SIN_TIPO" : espacio.getTipoVehiculo().getNombre(),
            LinkedHashMap::new,
            Collectors.toList()));

    List<String> columnas = List.of("tipoVehiculo", "totalEspacios", "ocupados", "libres", "porcentajeOcupacion");
    List<ReporteTablaFilaDTO> filas = porTipo.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> {
            String tipo = entry.getKey();
            List<Espacio> espacios = entry.getValue();
            long total = espacios.size();
            long ocupados = espacios.stream().filter(this::estaOcupado).count();
            long libres = Math.max(0, total - ocupados);
            double porcentaje = total == 0 ? 0.0d : (ocupados * 100.0d) / total;

            Map<String, String> row = new LinkedHashMap<>();
            row.put("tipoVehiculo", tipo);
            row.put("totalEspacios", String.valueOf(total));
            row.put("ocupados", String.valueOf(ocupados));
            row.put("libres", String.valueOf(libres));
            row.put("porcentajeOcupacion", String.format("%.2f", porcentaje));
            return new ReporteTablaFilaDTO(row);
        })
        .toList();

    return new ReporteTablaResponseDTO("Ocupacion por tipo de vehiculo", columnas, filas, (long) filas.size());
    }

    @Transactional(readOnly = true)
    @Cacheable("reportesCapacidad")
    public ReporteResumenKpiResponseDTO obtenerCapacidadActivaInactiva() {
    long activos = espacioRepository.findAllByActivoTrueOrderByIdAsc().size();
    long inactivos = espacioRepository.findAllByActivoFalseOrderByIdAsc().size();
    long total = activos + inactivos;

    List<ReporteKpiDTO> kpis = List.of(
        new ReporteKpiDTO("CAP_TOTAL", "Capacidad total", BigDecimal.valueOf(total), "espacios"),
        new ReporteKpiDTO("CAP_ACTIVA", "Activa", BigDecimal.valueOf(activos), "espacios"),
        new ReporteKpiDTO("CAP_INACTIVA", "Inactiva", BigDecimal.valueOf(inactivos), "espacios"));

    return new ReporteResumenKpiResponseDTO("Capacidad activa/inactiva", kpis);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerUtilizacionBasicaPorEspacio(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
    RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
    List<Espacio> espacios = espacioRepository.findAll();
    List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
        rango.fechaDesde(),
        rango.fechaHasta());

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
    public ReporteConsultaPlacaResponseDTO obtenerConsultaPorPlaca(String placa) {
    String placaNormalizada = normalizarTexto(placa).toUpperCase();
    if (placaNormalizada.isBlank()) {
        throw new IllegalArgumentException("La placa es obligatoria para realizar la consulta");
    }

    List<Ticket> tickets = ticketRepository.findAllByPlacaIgnoreCaseOrderByHoraEntradaDesc(placaNormalizada);
    List<Reserva> reservas = reservaRepository.findAllByPlacaIgnoreCaseOrderByFechaCreacionDesc(placaNormalizada);

    List<String> columnasTickets = List.of(
        "codigoTicket",
        "estado",
        "tipoVehiculo",
        "codigoEspacio",
        "horaEntrada",
        "horaSalida");
    List<ReporteTablaFilaDTO> filasTickets = tickets.stream()
        .map(ticket -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("codigoTicket", ticket.getCodigoTicket());
            row.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
            row.put("tipoVehiculo", ticket.getTipoVehiculo() == null ? "-" : ticket.getTipoVehiculo().getNombre());
            row.put("codigoEspacio", ticket.getEspacio() == null ? "-" : ticket.getEspacio().getCodigoEspacio());
            row.put("horaEntrada", formatDateTime(ticket.getHoraEntrada()));
            row.put("horaSalida", formatDateTime(ticket.getHoraSalida()));
            return new ReporteTablaFilaDTO(row);
        })
        .toList();

    List<String> columnasReservas = List.of(
        "codigoReserva",
        "estado",
        "tipoVehiculo",
        "codigoEspacio",
        "horaInicio",
        "horaFin");
    List<ReporteTablaFilaDTO> filasReservas = reservas.stream()
        .map(reserva -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("codigoReserva", reserva.getCodigoReserva());
            row.put("estado", reserva.getEstado() == null ? "-" : reserva.getEstado().getNombre());
            row.put("tipoVehiculo", reserva.getTipoVehiculo() == null ? "-" : reserva.getTipoVehiculo().getNombre());
            row.put("codigoEspacio", reserva.getEspacio() == null ? "-" : reserva.getEspacio().getCodigoEspacio());
            row.put("horaInicio", formatDateTime(reserva.getHoraInicio()));
            row.put("horaFin", formatDateTime(reserva.getHoraFin()));
            return new ReporteTablaFilaDTO(row);
        })
        .toList();

    ReporteTablaResponseDTO ticketsResponse = new ReporteTablaResponseDTO(
        "Tickets relacionados",
        columnasTickets,
        filasTickets,
        (long) filasTickets.size());

    ReporteTablaResponseDTO reservasResponse = new ReporteTablaResponseDTO(
        "Reservas relacionadas",
        columnasReservas,
        filasReservas,
        (long) filasReservas.size());

    return new ReporteConsultaPlacaResponseDTO(placaNormalizada, ticketsResponse, reservasResponse);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerConsultaPorCodigoTicket(String codigoTicket) {
    String codigoNormalizado = normalizarTexto(codigoTicket);
    if (codigoNormalizado.isBlank()) {
        throw new IllegalArgumentException("El codigo de ticket es obligatorio para realizar la consulta");
    }

    Ticket ticket = ticketRepository.findByCodigoTicketIgnoreCase(codigoNormalizado)
        .orElseThrow(() -> new NoSuchElementException("Ticket no encontrado"));

    List<String> columnas = List.of(
        "codigoTicket",
        "placa",
        "estado",
        "tipoVehiculo",
        "codigoEspacio",
        "horaEntrada",
        "horaSalida",
        "montoTotal");
    Map<String, String> row = new LinkedHashMap<>();
    row.put("codigoTicket", ticket.getCodigoTicket());
    row.put("placa", ticket.getPlaca());
    row.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
    row.put("tipoVehiculo", ticket.getTipoVehiculo() == null ? "-" : ticket.getTipoVehiculo().getNombre());
    row.put("codigoEspacio", ticket.getEspacio() == null ? "-" : ticket.getEspacio().getCodigoEspacio());
    row.put("horaEntrada", formatDateTime(ticket.getHoraEntrada()));
    row.put("horaSalida", formatDateTime(ticket.getHoraSalida()));
    row.put("montoTotal", ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().toPlainString());

    return new ReporteTablaResponseDTO(
        "Consulta por codigo de ticket",
        columnas,
        List.of(new ReporteTablaFilaDTO(row)),
        1L);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerConsultaPorCodigoReserva(String codigoReserva) {
    String codigoNormalizado = normalizarTexto(codigoReserva);
    if (codigoNormalizado.isBlank()) {
        throw new IllegalArgumentException("El codigo de reserva es obligatorio para realizar la consulta");
    }

    Reserva reserva = reservaRepository.findByCodigoReservaIgnoreCase(codigoNormalizado)
        .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada"));

    List<String> columnas = List.of(
        "codigoReserva",
        "placa",
        "estado",
        "tipoVehiculo",
        "codigoEspacio",
        "horaInicio",
        "horaFin",
        "motivoCancelacion");
    Map<String, String> row = new LinkedHashMap<>();
    row.put("codigoReserva", reserva.getCodigoReserva());
    row.put("placa", reserva.getPlaca());
    row.put("estado", reserva.getEstado() == null ? "-" : reserva.getEstado().getNombre());
    row.put("tipoVehiculo", reserva.getTipoVehiculo() == null ? "-" : reserva.getTipoVehiculo().getNombre());
    row.put("codigoEspacio", reserva.getEspacio() == null ? "-" : reserva.getEspacio().getCodigoEspacio());
    row.put("horaInicio", formatDateTime(reserva.getHoraInicio()));
    row.put("horaFin", formatDateTime(reserva.getHoraFin()));
    row.put("motivoCancelacion", normalizarMotivo(reserva.getMotivoCancelacion()));

    return new ReporteTablaResponseDTO(
        "Consulta por codigo de reserva",
        columnas,
        List.of(new ReporteTablaFilaDTO(row)),
        1L);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerHistorialConsolidadoCliente(String placa) {
        return obtenerHistorialConsolidadoCliente(placa, null, null);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerHistorialConsolidadoCliente(String placa, Integer page, Integer size) {
        String placaNormalizada = normalizarTexto(placa).toUpperCase(Locale.ROOT);
        if (placaNormalizada.isBlank()) {
            throw new IllegalArgumentException("La placa es obligatoria para consultar historial consolidado");
        }

        List<Ticket> tickets = ticketRepository.findAllByPlacaIgnoreCaseOrderByHoraEntradaDesc(placaNormalizada);
        List<Reserva> reservas = reservaRepository.findAllByPlacaIgnoreCaseOrderByFechaCreacionDesc(placaNormalizada);
        List<Pago> pagos = pagoRepository.findAllByTicket_PlacaIgnoreCase(placaNormalizada);

        record EventoHistorial(LocalDateTime fechaEvento, Map<String, String> fila) {
        }

        List<EventoHistorial> eventos = new java.util.ArrayList<>();

        for (Reserva reserva : reservas) {
            Map<String, String> filaCreacion = new LinkedHashMap<>();
            filaCreacion.put("fechaEvento", formatDateTime(reserva.getFechaCreacion()));
            filaCreacion.put("tipoEvento", "RESERVA_CREADA");
            filaCreacion.put("codigo", reserva.getCodigoReserva());
            filaCreacion.put("estado", reserva.getEstado() == null ? "-" : reserva.getEstado().getNombre());
            filaCreacion.put("detalle", "Inicio: " + formatDateTime(reserva.getHoraInicio()));
            filaCreacion.put("monto", "-");
            eventos.add(new EventoHistorial(reserva.getFechaCreacion(), filaCreacion));

            if (reserva.getHoraFin() != null && reserva.getEstado() != null
                    && ESTADO_RESERVA_CANCELADA.equalsIgnoreCase(reserva.getEstado().getNombre())) {
                Map<String, String> filaCancelacion = new LinkedHashMap<>();
                filaCancelacion.put("fechaEvento", formatDateTime(reserva.getHoraFin()));
                filaCancelacion.put("tipoEvento", "RESERVA_CANCELADA");
                filaCancelacion.put("codigo", reserva.getCodigoReserva());
                filaCancelacion.put("estado", reserva.getEstado().getNombre());
                filaCancelacion.put("detalle", "Motivo: " + normalizarMotivo(reserva.getMotivoCancelacion()));
                filaCancelacion.put("monto", "-");
                eventos.add(new EventoHistorial(reserva.getHoraFin(), filaCancelacion));
            }
        }

        for (Ticket ticket : tickets) {
            Map<String, String> filaEntrada = new LinkedHashMap<>();
            filaEntrada.put("fechaEvento", formatDateTime(ticket.getHoraEntrada()));
            filaEntrada.put("tipoEvento", "TICKET_ENTRADA");
            filaEntrada.put("codigo", ticket.getCodigoTicket());
            filaEntrada.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
            filaEntrada.put("detalle", "Espacio: " + (ticket.getEspacio() == null ? "-" : ticket.getEspacio().getCodigoEspacio()));
            filaEntrada.put("monto", "-");
            eventos.add(new EventoHistorial(ticket.getHoraEntrada(), filaEntrada));

            if (ticket.getHoraSalida() != null) {
                Map<String, String> filaSalida = new LinkedHashMap<>();
                filaSalida.put("fechaEvento", formatDateTime(ticket.getHoraSalida()));
                filaSalida.put("tipoEvento", "TICKET_SALIDA");
                filaSalida.put("codigo", ticket.getCodigoTicket());
                filaSalida.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
                filaSalida.put("detalle", "Salida registrada");
                filaSalida.put("monto", ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().toPlainString());
                eventos.add(new EventoHistorial(ticket.getHoraSalida(), filaSalida));
            }
        }

        for (Pago pago : pagos) {
            Map<String, String> filaPago = new LinkedHashMap<>();
            filaPago.put("fechaEvento", formatDateTime(pago.getHoraPago()));
            filaPago.put("tipoEvento", "PAGO");
            filaPago.put("codigo", pago.getTicket() == null ? "-" : pago.getTicket().getCodigoTicket());
            filaPago.put("estado", "COBRADO");
            filaPago.put("detalle", "Metodo: " + normalizarMetodoPago(pago.getMetodoPago()));
            filaPago.put("monto", pago.getMonto() == null ? "-" : pago.getMonto().toPlainString());
            eventos.add(new EventoHistorial(pago.getHoraPago(), filaPago));
        }

        List<String> columnas = List.of("fechaEvento", "tipoEvento", "codigo", "estado", "detalle", "monto");
        List<ReporteTablaFilaDTO> filas = eventos.stream()
                .sorted((a, b) -> {
                    LocalDateTime fa = a.fechaEvento() == null ? LocalDateTime.MIN : a.fechaEvento();
                    LocalDateTime fb = b.fechaEvento() == null ? LocalDateTime.MIN : b.fechaEvento();
                    return fb.compareTo(fa);
                })
                .map(evento -> new ReporteTablaFilaDTO(evento.fila()))
                .toList();

        return new ReporteTablaResponseDTO(
                "Historial consolidado por cliente (placa)",
                columnas,
            paginarFilas(filas, page, size),
                (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerTrazabilidadTicket(String codigoTicket) {
        String codigoNormalizado = normalizarTexto(codigoTicket);
        if (codigoNormalizado.isBlank()) {
            throw new IllegalArgumentException("El codigo de ticket es obligatorio para trazabilidad");
        }

        Ticket ticket = ticketRepository.findByCodigoTicketIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new NoSuchElementException("Ticket no encontrado"));

        Pago pago = ticket.getId() == null
            ? null
            : pagoRepository.findFirstByTicket_Id(ticket.getId()).orElse(null);

        List<String> columnas = List.of("paso", "fechaEvento", "tipoEvento", "estado", "detalle", "usuario");
        List<ReporteTablaFilaDTO> filas = new java.util.ArrayList<>();

        int paso = 1;

        Map<String, String> filaCreacion = new LinkedHashMap<>();
        filaCreacion.put("paso", String.valueOf(paso++));
        filaCreacion.put("fechaEvento", formatDateTime(ticket.getFechaCreacion()));
        filaCreacion.put("tipoEvento", "TICKET_CREADO");
        filaCreacion.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
        filaCreacion.put("detalle", "Codigo: " + ticket.getCodigoTicket() + " | Placa: " + ticket.getPlaca());
        filaCreacion.put("usuario", ticket.getCreadoPor() == null || ticket.getCreadoPor().getUsername() == null ? "-" : ticket.getCreadoPor().getUsername());
        filas.add(new ReporteTablaFilaDTO(filaCreacion));

        Map<String, String> filaEntrada = new LinkedHashMap<>();
        filaEntrada.put("paso", String.valueOf(paso++));
        filaEntrada.put("fechaEvento", formatDateTime(ticket.getHoraEntrada()));
        filaEntrada.put("tipoEvento", "ENTRADA");
        filaEntrada.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
        filaEntrada.put("detalle", "Espacio: " + (ticket.getEspacio() == null ? "-" : ticket.getEspacio().getCodigoEspacio()));
        filaEntrada.put("usuario", ticket.getCreadoPor() == null || ticket.getCreadoPor().getUsername() == null ? "-" : ticket.getCreadoPor().getUsername());
        filas.add(new ReporteTablaFilaDTO(filaEntrada));

        if (ticket.getHoraSalida() != null) {
            Map<String, String> filaSalida = new LinkedHashMap<>();
            filaSalida.put("paso", String.valueOf(paso++));
            filaSalida.put("fechaEvento", formatDateTime(ticket.getHoraSalida()));
            filaSalida.put("tipoEvento", "SALIDA");
            filaSalida.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
            filaSalida.put("detalle", "Monto total: " + (ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().toPlainString()));
            filaSalida.put("usuario", "-");
            filas.add(new ReporteTablaFilaDTO(filaSalida));
        }

        if (pago != null) {
            Map<String, String> filaPago = new LinkedHashMap<>();
            filaPago.put("paso", String.valueOf(paso));
            filaPago.put("fechaEvento", formatDateTime(pago.getHoraPago()));
            filaPago.put("tipoEvento", "PAGO");
            filaPago.put("estado", "COBRADO");
            filaPago.put("detalle", "Metodo: " + normalizarMetodoPago(pago.getMetodoPago()) + " | Monto: "
                    + (pago.getMonto() == null ? "-" : pago.getMonto().toPlainString()));
            filaPago.put("usuario", pago.getProcesadoPor() == null || pago.getProcesadoPor().getUsername() == null
                    ? "-"
                    : pago.getProcesadoPor().getUsername());
            filas.add(new ReporteTablaFilaDTO(filaPago));
        }

        return new ReporteTablaResponseDTO(
                "Trazabilidad de ticket",
                columnas,
                filas,
                (long) filas.size());
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerConsultasPorRangoMontos(
            BigDecimal montoDesde,
            BigDecimal montoHasta,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        return obtenerConsultasPorRangoMontos(montoDesde, montoHasta, fechaDesde, fechaHasta, null, null);
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerConsultasPorRangoMontos(
            BigDecimal montoDesde,
            BigDecimal montoHasta,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);

        BigDecimal desde = montoDesde == null ? BigDecimal.ZERO : montoDesde;
        BigDecimal hasta = montoHasta == null ? new BigDecimal("999999999") : montoHasta;

        if (desde.compareTo(BigDecimal.ZERO) < 0 || hasta.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El rango de montos no puede ser negativo");
        }
        if (hasta.compareTo(desde) < 0) {
            throw new IllegalArgumentException("montoHasta no puede ser menor que montoDesde");
        }

        List<Pago> pagos = obtenerPagosEnRango(rango).stream()
                .filter(pago -> pago.getMonto() != null)
                .filter(pago -> pago.getMonto().compareTo(desde) >= 0 && pago.getMonto().compareTo(hasta) <= 0)
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getHoraPago() == null ? LocalDateTime.MIN : a.getHoraPago();
                    LocalDateTime fb = b.getHoraPago() == null ? LocalDateTime.MIN : b.getHoraPago();
                    return fb.compareTo(fa);
                })
                .toList();

        List<String> columnas = List.of(
                "codigoTicket",
                "placa",
                "tipoVehiculo",
                "monto",
                "metodoPago",
                "horaPago",
                "procesadoPor");

        List<ReporteTablaFilaDTO> filas = pagos.stream()
                .map(pago -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("codigoTicket", pago.getTicket() == null ? "-" : pago.getTicket().getCodigoTicket());
                    row.put("placa", pago.getTicket() == null ? "-" : valorCsv(pago.getTicket().getPlaca()));
                    row.put("tipoVehiculo", pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null
                            ? "-"
                            : valorCsv(pago.getTicket().getTipoVehiculo().getNombre()));
                    row.put("monto", pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    row.put("metodoPago", normalizarMetodoPago(pago.getMetodoPago()));
                    row.put("horaPago", formatDateTime(pago.getHoraPago()));
                    row.put("procesadoPor", pago.getProcesadoPor() == null || pago.getProcesadoPor().getUsername() == null
                            ? "-"
                            : pago.getProcesadoPor().getUsername());
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO(
                "Consulta por rango de montos",
                columnas,
            paginarFilas(filas, page, size),
                (long) filas.size());
    }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerIngresosPorPeriodo(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String granularidad) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        String granularidadNormalizada = normalizarGranularidad(granularidad);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        Map<String, BigDecimal> ingresosPorPeriodo = pagos.stream()
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

        Map<String, BigDecimal> ingresosPorTipo = pagos.stream()
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
                Map<String, String> row = new LinkedHashMap<>();
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

        Map<String, BigDecimal> ingresosPorMetodo = pagos.stream()
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
                Map<String, String> row = new LinkedHashMap<>();
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

        Map<Integer, BigDecimal> ingresosPorHora = pagos.stream()
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
            .map(entry -> entry)
            .toList()
            .stream()
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

        @Transactional(readOnly = true)
        public ReporteComparativoResponseDTO obtenerComparativoEntradasSalidas(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String modoComparacion) {
        RangosComparacion rangos = resolverRangosComparacion(fechaDesde, fechaHasta, modoComparacion);

        long entradasActual = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rangos.actual().fechaDesde(),
            rangos.actual().fechaHasta()).size();
        long salidasActual = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rangos.actual().fechaDesde(),
            rangos.actual().fechaHasta()).size();

        long entradasComparado = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rangos.comparado().fechaDesde(),
            rangos.comparado().fechaHasta()).size();
        long salidasComparado = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rangos.comparado().fechaDesde(),
            rangos.comparado().fechaHasta()).size();

        List<ReporteComparativoItemDTO> items = List.of(
            construirComparativoItem(
                "Entradas",
                BigDecimal.valueOf(entradasActual),
                BigDecimal.valueOf(entradasComparado)),
            construirComparativoItem(
                "Salidas",
                BigDecimal.valueOf(salidasActual),
                BigDecimal.valueOf(salidasComparado)),
            construirComparativoItem(
                "Flujo neto",
                BigDecimal.valueOf(entradasActual - salidasActual),
                BigDecimal.valueOf(entradasComparado - salidasComparado)));

        return new ReporteComparativoResponseDTO(
            "Comparativo de entradas y salidas",
            "tickets",
            construirTextoPeriodo(rangos.actual()),
            construirTextoPeriodo(rangos.comparado()),
            items);
        }

        @Transactional(readOnly = true)
        public ReporteComparativoResponseDTO obtenerComparativoReservasPorEstado(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String modoComparacion) {
        RangosComparacion rangos = resolverRangosComparacion(fechaDesde, fechaHasta, modoComparacion);

        List<Reserva> reservasActual = obtenerReservasCreadasEnRango(rangos.actual());
        List<Reserva> reservasComparado = obtenerReservasCreadasEnRango(rangos.comparado());

        List<String> estados = List.of(
            ESTADO_RESERVA_PENDIENTE,
            ESTADO_RESERVA_ACTIVA,
            ESTADO_RESERVA_FINALIZADA,
            ESTADO_RESERVA_CANCELADA);

        List<ReporteComparativoItemDTO> items = estados.stream()
            .map(estado -> construirComparativoItem(
                estado,
                BigDecimal.valueOf(contarPorEstado(reservasActual, estado)),
                BigDecimal.valueOf(contarPorEstado(reservasComparado, estado))))
            .toList();

        return new ReporteComparativoResponseDTO(
            "Comparativo de reservas por estado",
            "reservas",
            construirTextoPeriodo(rangos.actual()),
            construirTextoPeriodo(rangos.comparado()),
            items);
        }

        @Transactional(readOnly = true)
        public ReporteComparativoResponseDTO obtenerComparativoOcupacionPorFranja(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String modoComparacion) {
        RangosComparacion rangos = resolverRangosComparacion(fechaDesde, fechaHasta, modoComparacion);

        List<Ticket> ticketsActual = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rangos.actual().fechaDesde(),
            rangos.actual().fechaHasta());
        List<Ticket> ticketsComparado = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rangos.comparado().fechaDesde(),
            rangos.comparado().fechaHasta());

        Map<Integer, Long> actualPorHora = ticketsActual.stream()
            .filter(ticket -> ticket.getHoraEntrada() != null)
            .collect(Collectors.groupingBy(ticket -> ticket.getHoraEntrada().getHour(), Collectors.counting()));
        Map<Integer, Long> comparadoPorHora = ticketsComparado.stream()
            .filter(ticket -> ticket.getHoraEntrada() != null)
            .collect(Collectors.groupingBy(ticket -> ticket.getHoraEntrada().getHour(), Collectors.counting()));

        List<ReporteComparativoItemDTO> items = java.util.stream.IntStream.range(0, 24)
            .mapToObj(hora -> construirComparativoItem(
                String.format("%02d:00", hora),
                BigDecimal.valueOf(actualPorHora.getOrDefault(hora, 0L)),
                BigDecimal.valueOf(comparadoPorHora.getOrDefault(hora, 0L))))
            .toList();

        return new ReporteComparativoResponseDTO(
            "Comparativo de ocupacion por franja horaria",
            "tickets",
            construirTextoPeriodo(rangos.actual()),
            construirTextoPeriodo(rangos.comparado()),
            items);
        }

        @Transactional(readOnly = true)
        public ReporteResumenKpiResponseDTO obtenerTasaConversionReservaIngreso(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> reservasProgramadas = obtenerReservasProgramadasEnRango(rango);
        List<Pago> pagos = obtenerPagosEnRango(rango);

        long convertidas = reservasProgramadas.stream()
            .filter(reserva -> existePagoAsociableAReserva(reserva, pagos))
            .count();
        long total = reservasProgramadas.size();
        long noConvertidas = Math.max(0L, total - convertidas);

        BigDecimal tasaConversion = calcularPorcentaje(convertidas, total);

        List<ReporteKpiDTO> kpis = List.of(
            new ReporteKpiDTO("RES_PROGRAMADAS", "Reservas programadas", BigDecimal.valueOf(total), "reservas"),
            new ReporteKpiDTO("RES_CONVERTIDAS", "Convertidas a ingreso", BigDecimal.valueOf(convertidas), "reservas"),
            new ReporteKpiDTO("RES_NO_CONVERTIDAS", "No convertidas", BigDecimal.valueOf(noConvertidas), "reservas"),
            new ReporteKpiDTO("TASA_CONVERSION", "Tasa conversion", tasaConversion, "%"));

        return new ReporteResumenKpiResponseDTO("Conversion reserva -> ingreso efectivo", kpis);
        }

        @Transactional(readOnly = true)
        public ReporteResumenKpiResponseDTO obtenerTasaNoShowReservas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> reservasProgramadas = obtenerReservasProgramadasEnRango(rango);

        long noShow = reservasProgramadas.stream()
            .filter(this::esReservaNoShow)
            .count();
        long total = reservasProgramadas.size();

        BigDecimal tasaNoShow = calcularPorcentaje(noShow, total);

        List<ReporteKpiDTO> kpis = List.of(
            new ReporteKpiDTO("RES_PROGRAMADAS", "Reservas programadas", BigDecimal.valueOf(total), "reservas"),
            new ReporteKpiDTO("RES_NO_SHOW", "No-show", BigDecimal.valueOf(noShow), "reservas"),
            new ReporteKpiDTO("TASA_NO_SHOW", "Tasa no-show", tasaNoShow, "%"));

        return new ReporteResumenKpiResponseDTO("Tasa de no-show de reservas", kpis);
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerCancelacionesPorOperador(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

        Map<String, Long> cancelacionesPorOperador = canceladas.stream()
            .collect(Collectors.groupingBy(
                reserva -> {
                    if (reserva.getCanceladoPor() == null || reserva.getCanceladoPor().getUsername() == null
                        || reserva.getCanceladoPor().getUsername().isBlank()) {
                    return "SIN_USUARIO";
                    }
                    return reserva.getCanceladoPor().getUsername();
                },
                Collectors.counting()));

        List<String> columnas = List.of("operador", "cancelaciones");
        List<ReporteTablaFilaDTO> filas = cancelacionesPorOperador.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .map(entry -> {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("operador", entry.getKey());
                row.put("cancelaciones", String.valueOf(entry.getValue()));
                return new ReporteTablaFilaDTO(row);
            })
            .toList();

        return new ReporteTablaResponseDTO("Cancelaciones por usuario operador", columnas, filas, (long) filas.size());
        }

        @Transactional(readOnly = true)
        public ReporteTablaResponseDTO obtenerTiempoPromedioOcupacionPorTipo(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta()).stream()
            .filter(ticket -> ticket.getHoraEntrada() != null)
            .filter(ticket -> ticket.getHoraSalida() != null)
            .toList();

        Map<String, List<Long>> minutosPorTipo = tickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> {
                    if (ticket.getEspacio() == null || ticket.getEspacio().getTipoVehiculo() == null
                        || ticket.getEspacio().getTipoVehiculo().getNombre() == null) {
                    return "SIN_TIPO";
                    }
                    return ticket.getEspacio().getTipoVehiculo().getNombre();
                },
                Collectors.mapping(ticket -> calcularEstadiaMinutos(ticket, ticket.getHoraSalida()), Collectors.toList())));

        List<String> columnas = List.of("tipoEspacio", "ticketsFinalizados", "minutosPromedio", "horasPromedio");
        List<ReporteTablaFilaDTO> filas = minutosPorTipo.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<Long> minutos = entry.getValue();
                double promedioMinutos = minutos.isEmpty()
                    ? 0.0d
                    : minutos.stream().mapToLong(Long::longValue).average().orElse(0.0d);

                Map<String, String> row = new LinkedHashMap<>();
                row.put("tipoEspacio", entry.getKey());
                row.put("ticketsFinalizados", String.valueOf(minutos.size()));
                row.put("minutosPromedio", String.format("%.2f", promedioMinutos));
                row.put("horasPromedio", String.format("%.2f", promedioMinutos / 60.0d));
                return new ReporteTablaFilaDTO(row);
            })
            .toList();

        return new ReporteTablaResponseDTO("Tiempo promedio de ocupacion por tipo de espacio", columnas, filas, (long) filas.size());
        }

        @Transactional(readOnly = true)
        public byte[] exportarTicketsEnRangoCsv(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta());

        String[] headers = {
            "codigoTicket",
            "placa",
            "tipoVehiculo",
            "codigoEspacio",
            "estado",
            "horaEntrada",
            "horaSalida",
            "montoTotal"
        };

        return generarCsv(headers, tickets.stream()
            .map(ticket -> List.of(
                valorCsv(ticket.getCodigoTicket()),
                valorCsv(ticket.getPlaca()),
                ticket.getTipoVehiculo() == null ? "-" : valorCsv(ticket.getTipoVehiculo().getNombre()),
                ticket.getEspacio() == null ? "-" : valorCsv(ticket.getEspacio().getCodigoEspacio()),
                ticket.getEstado() == null ? "-" : valorCsv(ticket.getEstado().getNombre()),
                formatDateTime(ticket.getHoraEntrada()),
                formatDateTime(ticket.getHoraSalida()),
                ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().toPlainString()))
            .toList());
        }

        @Transactional(readOnly = true)
        public byte[] exportarReservasEnRangoCsv(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> reservas = obtenerReservasCreadasEnRango(rango);

        String[] headers = {
            "codigoReserva",
            "placa",
            "cliente",
            "tipoVehiculo",
            "codigoEspacio",
            "estado",
            "horaInicio",
            "horaFin",
            "fechaCreacion"
        };

        return generarCsv(headers, reservas.stream()
            .map(reserva -> List.of(
                valorCsv(reserva.getCodigoReserva()),
                valorCsv(reserva.getPlaca()),
                valorCsv(reserva.getClienteNombreCompleto()),
                reserva.getTipoVehiculo() == null ? "-" : valorCsv(reserva.getTipoVehiculo().getNombre()),
                reserva.getEspacio() == null ? "-" : valorCsv(reserva.getEspacio().getCodigoEspacio()),
                reserva.getEstado() == null ? "-" : valorCsv(reserva.getEstado().getNombre()),
                formatDateTime(reserva.getHoraInicio()),
                formatDateTime(reserva.getHoraFin()),
                formatDateTime(reserva.getFechaCreacion())))
            .toList());
        }

        @Transactional(readOnly = true)
        public byte[] exportarCancelacionesConMotivoCsv(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

        String[] headers = {
            "codigoReserva",
            "placa",
            "cliente",
            "codigoEspacio",
            "horaInicio",
            "horaCancelacion",
            "motivoCancelacion"
        };

        return generarCsv(headers, canceladas.stream()
            .map(reserva -> List.of(
                valorCsv(reserva.getCodigoReserva()),
                valorCsv(reserva.getPlaca()),
                valorCsv(reserva.getClienteNombreCompleto()),
                reserva.getEspacio() == null ? "-" : valorCsv(reserva.getEspacio().getCodigoEspacio()),
                formatDateTime(reserva.getHoraInicio()),
                formatDateTime(reserva.getHoraFin()),
                normalizarMotivo(reserva.getMotivoCancelacion())))
            .toList());
        }

        public String construirNombreArchivoCsv(String prefijo) {
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMATTER);
        return prefijo + "_" + timestamp + ".csv";
        }

            @Transactional(readOnly = true)
            public byte[] exportarResumenOperativoDiarioPdf(LocalDateTime fechaDesde, LocalDateTime fechaHasta, String granularidad) {
            RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
            String granularidadNormalizada = normalizarGranularidad(granularidad);

            List<Ticket> entradasEnRango = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta());
            List<Ticket> salidasEnRango = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta());
            List<Reserva> reservasCreadasEnRango = obtenerReservasCreadasEnRango(rango);
            List<Reserva> cancelacionesEnRango = obtenerReservasCanceladasEnRango(rango);

            long entradas = entradasEnRango.size();
            long salidas = salidasEnRango.size();
            long activos = ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getEstado() != null)
                .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                .count();
            long reservasCreadas = reservasCreadasEnRango.size();
            long cancelaciones = cancelacionesEnRango.size();

            Map<String, BigDecimal> entradasPorPeriodo = agruparConteoPorPeriodoTicket(
                entradasEnRango,
                Ticket::getHoraEntrada,
                granularidadNormalizada);
            Map<String, BigDecimal> salidasPorPeriodo = agruparConteoPorPeriodoTicket(
                salidasEnRango,
                Ticket::getHoraSalida,
                granularidadNormalizada);
            Map<String, BigDecimal> reservasPorPeriodo = agruparConteoPorPeriodoReserva(
                reservasCreadasEnRango,
                Reserva::getFechaCreacion,
                granularidadNormalizada);
            Map<String, BigDecimal> cancelacionesPorPeriodo = agruparConteoPorPeriodoReserva(
                cancelacionesEnRango,
                Reserva::getHoraFin,
                granularidadNormalizada);

            List<String> headers = List.of("Indicador", "Valor");
            List<List<String>> rows = List.of(
                List.of("Entradas en rango", String.valueOf(entradas)),
                List.of("Salidas en rango", String.valueOf(salidas)),
                List.of("Flujo neto en rango", String.valueOf(entradas - salidas)),
                List.of("Tickets activos actuales", String.valueOf(activos)),
                List.of("Reservas creadas en rango", String.valueOf(reservasCreadas)),
                List.of("Cancelaciones en rango", String.valueOf(cancelaciones)),
                List.of("Granularidad aplicada", granularidadNormalizada));

            return generarPdfProfesional(
                "Resumen operativo",
                "Periodo: " + construirTextoPeriodo(rango) + " | Granularidad: " + granularidadNormalizada,
                List.of(new PdfTablaSeccion("Indicadores clave", headers, rows)),
                List.of(
                    new PdfGraficaSeccion("Entradas por periodo", "eventos", toTopBarItems(entradasPorPeriodo, 12)),
                    new PdfGraficaSeccion("Salidas por periodo", "eventos", toTopBarItems(salidasPorPeriodo, 12)),
                    new PdfGraficaSeccion("Reservas creadas por periodo", "eventos", toTopBarItems(reservasPorPeriodo, 12)),
                    new PdfGraficaSeccion("Cancelaciones por periodo", "eventos", toTopBarItems(cancelacionesPorPeriodo, 12))));
            }

            @Transactional(readOnly = true)
            public byte[] exportarCancelacionesConMotivoPdf(LocalDateTime fechaDesde, LocalDateTime fechaHasta, String granularidad) {
            RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
            String granularidadNormalizada = normalizarGranularidad(granularidad);
            List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

            List<String> headers = List.of(
                "Codigo",
                "Placa",
                "Cliente",
                "Espacio",
                "Hora inicio",
                "Hora cancelacion",
                "Motivo");

            List<List<String>> rows = canceladas.stream()
                .map(reserva -> List.of(
                    valorCsv(reserva.getCodigoReserva()),
                    valorCsv(reserva.getPlaca()),
                    valorCsv(reserva.getClienteNombreCompleto()),
                    reserva.getEspacio() == null ? "-" : valorCsv(reserva.getEspacio().getCodigoEspacio()),
                    formatDateTime(reserva.getHoraInicio()),
                    formatDateTime(reserva.getHoraFin()),
                    normalizarMotivo(reserva.getMotivoCancelacion())))
                .toList();

            long sinMotivo = canceladas.stream()
                .map(Reserva::getMotivoCancelacion)
                .map(this::normalizarMotivo)
                .filter("SIN_MOTIVO_REGISTRADO"::equals)
                .count();
            long conMotivo = canceladas.size() - sinMotivo;

            List<PdfBarItem> motivosChart = canceladas.stream()
                .collect(Collectors.groupingBy(
                    reserva -> normalizarMotivo(reserva.getMotivoCancelacion()),
                    Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .map(entry -> new PdfBarItem(entry.getKey(), BigDecimal.valueOf(entry.getValue())))
                .toList();

            Map<String, BigDecimal> cancelacionesPorPeriodo = agruparConteoPorPeriodoReserva(
                canceladas,
                Reserva::getHoraFin,
                granularidadNormalizada);

            String subtitulo = "Rango: " + formatDateTime(rango.fechaDesde()) + " a " + formatDateTime(rango.fechaHasta())
                + " | Granularidad: " + granularidadNormalizada;
            List<String> summaryHeaders = List.of("Indicador", "Valor");
            List<List<String>> summaryRows = List.of(
                List.of("Cancelaciones totales", String.valueOf(canceladas.size())),
                List.of("Con motivo detallado", String.valueOf(conMotivo)),
                List.of("Sin motivo registrado", String.valueOf(sinMotivo)),
                List.of("Granularidad aplicada", granularidadNormalizada));

            return generarPdfProfesional(
                "Cancelaciones de reservas con motivo",
                subtitulo,
                List.of(
                    new PdfTablaSeccion("Resumen de cancelaciones", summaryHeaders, summaryRows),
                    new PdfTablaSeccion("Detalle de cancelaciones", headers, rows)),
                List.of(
                    new PdfGraficaSeccion("Top motivos de cancelacion", "casos", motivosChart),
                    new PdfGraficaSeccion("Cancelaciones por periodo", "casos", toTopBarItems(cancelacionesPorPeriodo, 12))));
            }

                public byte[] exportarOperativosAvanzadoCsv(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo) {
                return exportarOperativosAvanzadoCsvImpl(fechaDesde, fechaHasta, usuarioId, tipoVehiculo);
                }

                public byte[] exportarFinancierosAvanzadoCsv(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo) {
                return exportarFinancierosAvanzadoCsvImpl(fechaDesde, fechaHasta, usuarioId, tipoVehiculo);
                }

                public byte[] exportarResumenEjecutivoPdf(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo,
                    String granularidad) {
                return exportarResumenEjecutivoPdfImpl(fechaDesde, fechaHasta, usuarioId, tipoVehiculo, granularidad);
                }

                public String construirNombreArchivoEstandar(String modulo, String tipoReporte, String extension) {
                return construirNombreArchivoEstandarImpl(modulo, tipoReporte, extension);
                }

            @Transactional(readOnly = true)
                private byte[] exportarOperativosAvanzadoCsvImpl(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo) {
            RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
            String tipoNormalizado = normalizarTexto(tipoVehiculo).toUpperCase(Locale.ROOT);

            List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .filter(ticket -> filtraUsuarioTicket(ticket, usuarioId))
                .filter(ticket -> filtraTipoVehiculoTicket(ticket, tipoNormalizado))
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                    LocalDateTime fb = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                    return fb.compareTo(fa);
                })
                .toList();

            String[] headers = {
                "codigoTicket",
                "placa",
                "tipoVehiculo",
                "codigoEspacio",
                "usuario",
                "horaEntrada",
                "horaSalida",
                "minutosEstadia",
                "montoTotal",
                "estado"
            };

            return generarCsv(headers, tickets.stream()
                .map(ticket -> List.of(
                    valorCsv(ticket.getCodigoTicket()),
                    valorCsv(ticket.getPlaca()),
                    ticket.getTipoVehiculo() == null ? "-" : valorCsv(ticket.getTipoVehiculo().getNombre()),
                    ticket.getEspacio() == null ? "-" : valorCsv(ticket.getEspacio().getCodigoEspacio()),
                    obtenerEtiquetaUsuario(ticket),
                    formatDateTime(ticket.getHoraEntrada()),
                    formatDateTime(ticket.getHoraSalida()),
                    String.valueOf(calcularEstadiaMinutos(ticket, ticket.getHoraSalida())),
                    ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    ticket.getEstado() == null ? "-" : valorCsv(ticket.getEstado().getNombre())))
                .toList());
            }

            @Transactional(readOnly = true)
                private byte[] exportarFinancierosAvanzadoCsvImpl(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo) {
            RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
            String tipoNormalizado = normalizarTexto(tipoVehiculo).toUpperCase(Locale.ROOT);

            List<Pago> pagos = obtenerPagosEnRango(rango).stream()
                .filter(pago -> filtraUsuarioPago(pago, usuarioId))
                .filter(pago -> filtraTipoVehiculoPago(pago, tipoNormalizado))
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getHoraPago() == null ? LocalDateTime.MIN : a.getHoraPago();
                    LocalDateTime fb = b.getHoraPago() == null ? LocalDateTime.MIN : b.getHoraPago();
                    return fb.compareTo(fa);
                })
                .toList();

            String[] headers = {
                "codigoTicket",
                "placa",
                "tipoVehiculo",
                "monto",
                "metodoPago",
                "horaPago",
                "procesadoPor",
                "usuarioCreacionTicket"
            };

            return generarCsv(headers, pagos.stream()
                .map(pago -> List.of(
                    pago.getTicket() == null ? "-" : valorCsv(pago.getTicket().getCodigoTicket()),
                    pago.getTicket() == null ? "-" : valorCsv(pago.getTicket().getPlaca()),
                    pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null ? "-"
                        : valorCsv(pago.getTicket().getTipoVehiculo().getNombre()),
                    pago.getMonto() == null ? "-" : pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    normalizarMetodoPago(pago.getMetodoPago()),
                    formatDateTime(pago.getHoraPago()),
                    pago.getProcesadoPor() == null || pago.getProcesadoPor().getUsername() == null ? "-"
                        : valorCsv(pago.getProcesadoPor().getUsername()),
                    pago.getTicket() == null || pago.getTicket().getCreadoPor() == null || pago.getTicket().getCreadoPor().getUsername() == null
                        ? "-"
                        : valorCsv(pago.getTicket().getCreadoPor().getUsername())))
                .toList());
            }

            @Transactional(readOnly = true)
                private byte[] exportarResumenEjecutivoPdfImpl(
                    LocalDateTime fechaDesde,
                    LocalDateTime fechaHasta,
                    Long usuarioId,
                    String tipoVehiculo,
                    String granularidad) {
            RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
            String tipoNormalizado = normalizarTexto(tipoVehiculo).toUpperCase(Locale.ROOT);
            String granularidadNormalizada = normalizarGranularidad(granularidad);

            List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .filter(ticket -> filtraUsuarioTicket(ticket, usuarioId))
                .filter(ticket -> filtraTipoVehiculoTicket(ticket, tipoNormalizado))
                .toList();

            List<Pago> pagos = obtenerPagosEnRango(rango).stream()
                .filter(pago -> filtraUsuarioPago(pago, usuarioId))
                .filter(pago -> filtraTipoVehiculoPago(pago, tipoNormalizado))
                .toList();

            BigDecimal ingresos = pagos.stream()
                .map(Pago::getMonto)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);

            long ticketsActivos = tickets.stream()
                .filter(ticket -> ticket.getEstado() != null)
                .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                .count();

            long ticketsFinalizados = tickets.stream()
                .filter(ticket -> ticket.getHoraSalida() != null)
                .count();

            Map<String, BigDecimal> ingresosPorTipo = pagos.stream()
                .collect(Collectors.groupingBy(
                    pago -> pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null
                        ? "SIN_TIPO"
                        : valorCsv(pago.getTicket().getTipoVehiculo().getNombre()),
                    Collectors.mapping(
                        pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

            Map<String, BigDecimal> ingresosPorMetodo = pagos.stream()
                .collect(Collectors.groupingBy(
                    pago -> normalizarMetodoPago(pago.getMetodoPago()),
                    Collectors.mapping(
                        pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

            Map<String, BigDecimal> ingresosPorPeriodo = pagos.stream()
                .filter(pago -> pago.getHoraPago() != null)
                .collect(Collectors.groupingBy(
                    pago -> construirEtiquetaPeriodo(pago.getHoraPago(), granularidadNormalizada),
                    Collectors.mapping(
                        pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

            String subtitulo = "Periodo: " + formatDateTime(rango.fechaDesde()) + " a " + formatDateTime(rango.fechaHasta())
                + " | Usuario: " + (usuarioId == null ? "TODOS" : usuarioId)
                + " | Tipo vehiculo: " + (tipoNormalizado.isBlank() ? "TODOS" : tipoNormalizado)
                + " | Granularidad: " + granularidadNormalizada;

            List<String> headers = List.of("Indicador", "Valor");
            List<List<String>> rows = List.of(
                List.of("Ingresos totales", ingresos.toPlainString() + " " + MONEDA_DEFAULT),
                List.of("Tickets en periodo", String.valueOf(tickets.size())),
                List.of("Tickets finalizados", String.valueOf(ticketsFinalizados)),
                List.of("Tickets activos", String.valueOf(ticketsActivos)),
                List.of("Pagos procesados", String.valueOf(pagos.size())),
                List.of("Granularidad aplicada", granularidadNormalizada));

            return generarPdfProfesional(
                "Resumen ejecutivo de reportes",
                subtitulo,
                List.of(new PdfTablaSeccion("KPIs ejecutivos", headers, rows)),
                List.of(
                    new PdfGraficaSeccion("Ingresos por tipo de vehiculo", MONEDA_DEFAULT, toTopBarItems(ingresosPorTipo, 8)),
                    new PdfGraficaSeccion("Ingresos por metodo de pago", MONEDA_DEFAULT, toTopBarItems(ingresosPorMetodo, 8)),
                    new PdfGraficaSeccion("Ingresos por periodo", MONEDA_DEFAULT, toTopBarItems(ingresosPorPeriodo, 12))));
            }

            public String construirNombreArchivoPdf(String prefijo) {
            String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMATTER);
            return prefijo + "_" + timestamp + ".pdf";
            }

            private String construirNombreArchivoEstandarImpl(String modulo, String tipoReporte, String extension) {
            String moduloNormalizado = normalizarTexto(modulo).replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase(Locale.ROOT);
            String tipoNormalizado = normalizarTexto(tipoReporte).replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase(Locale.ROOT);
            String ext = normalizarTexto(extension).toLowerCase(Locale.ROOT);
            if (moduloNormalizado.isBlank()) {
                moduloNormalizado = "reportes";
            }
            if (tipoNormalizado.isBlank()) {
                tipoNormalizado = "general";
            }
            if (ext.isBlank()) {
                ext = "csv";
            }
            String timestamp = LocalDateTime.now().format(FILE_STANDARD_TIMESTAMP_FORMATTER);
            return moduloNormalizado + "_" + tipoNormalizado + "_" + timestamp + "." + ext;
            }

        private ReporteSerieTemporalResponseDTO construirSeriePorHora(
            List<Ticket> tickets,
            Function<Ticket, LocalDateTime> extractor,
            String titulo,
            String unidad) {
        long[] conteoPorHora = agruparPorHora(tickets, extractor);
        List<ReporteSerieTemporalItemDTO> items = java.util.stream.IntStream.range(0, 24)
            .mapToObj(hora -> new ReporteSerieTemporalItemDTO(
                String.format("%02d:00", hora),
                BigDecimal.valueOf(conteoPorHora[hora])))
            .toList();

        return new ReporteSerieTemporalResponseDTO(titulo, unidad, items);
        }

        private long[] agruparPorHora(List<Ticket> tickets, Function<Ticket, LocalDateTime> extractor) {
        long[] conteoPorHora = new long[24];
        tickets.stream()
            .map(extractor)
            .filter(java.util.Objects::nonNull)
            .forEach(fechaHora -> conteoPorHora[fechaHora.getHour()]++);
        return conteoPorHora;
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

        private ReporteTablaResponseDTO paginarTabla(
                String titulo,
                List<String> columnas,
                List<ReporteTablaFilaDTO> filas,
                Integer page,
                Integer size) {
            return new ReporteTablaResponseDTO(
                    titulo,
                    columnas,
                    paginarFilas(filas, page, size),
                    (long) filas.size());
        }

        private List<ReporteTablaFilaDTO> paginarFilas(List<ReporteTablaFilaDTO> filas, Integer page, Integer size) {
            if (filas == null || filas.isEmpty()) {
                return List.of();
            }

            int pageValue = page == null ? DEFAULT_PAGE : page;
            int sizeValue = size == null ? DEFAULT_SIZE : size;
            validarPaginacion(pageValue, sizeValue);

            int fromIndex = pageValue * sizeValue;
            if (fromIndex >= filas.size()) {
                return List.of();
            }

            int toIndex = Math.min(fromIndex + sizeValue, filas.size());
            return filas.subList(fromIndex, toIndex);
        }

        private void validarPaginacion(int page, int size) {
            if (page < 0) {
                throw new IllegalArgumentException("page no puede ser negativo");
            }
            if (size < 1 || size > MAX_SIZE) {
                throw new IllegalArgumentException("size debe estar entre 1 y " + MAX_SIZE);
            }
        }

        private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DATE_TIME_FORMATTER);
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

        private boolean estaOcupado(Espacio espacio) {
            if (espacio == null || espacio.getEstado() == null || espacio.getEstado().getNombre() == null) {
                return false;
            }
            return ESTADOS_OCUPADOS_ESPACIO.contains(espacio.getEstado().getNombre().trim().toUpperCase());
        }

        private boolean filtraUsuarioTicket(Ticket ticket, Long usuarioId) {
            if (usuarioId == null) {
                return true;
            }
            if (ticket == null || ticket.getCreadoPor() == null || ticket.getCreadoPor().getId() == null) {
                return false;
            }
            return usuarioId.equals(ticket.getCreadoPor().getId());
        }

        private boolean filtraTipoVehiculoTicket(Ticket ticket, String tipoVehiculoNormalizado) {
            if (tipoVehiculoNormalizado == null || tipoVehiculoNormalizado.isBlank()) {
                return true;
            }
            if (ticket == null || ticket.getTipoVehiculo() == null || ticket.getTipoVehiculo().getNombre() == null) {
                return false;
            }
            return tipoVehiculoNormalizado.equals(ticket.getTipoVehiculo().getNombre().trim().toUpperCase(Locale.ROOT));
        }

        private boolean filtraUsuarioPago(Pago pago, Long usuarioId) {
            if (usuarioId == null) {
                return true;
            }
            if (pago == null || pago.getProcesadoPor() == null || pago.getProcesadoPor().getId() == null) {
                return false;
            }
            return usuarioId.equals(pago.getProcesadoPor().getId());
        }

        private boolean filtraTipoVehiculoPago(Pago pago, String tipoVehiculoNormalizado) {
            if (tipoVehiculoNormalizado == null || tipoVehiculoNormalizado.isBlank()) {
                return true;
            }
            if (pago == null || pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null
                    || pago.getTicket().getTipoVehiculo().getNombre() == null) {
                return false;
            }
            return tipoVehiculoNormalizado.equals(
                    pago.getTicket().getTipoVehiculo().getNombre().trim().toUpperCase(Locale.ROOT));
        }

        private List<Pago> obtenerPagosEnRango(RangoFechas rango) {
            return pagoRepository.findAllByHoraPagoGreaterThanEqualAndHoraPagoLessThan(
                rango.fechaDesde(),
                rango.fechaHasta());
        }

        private Map<String, BigDecimal> agruparConteoPorPeriodoTicket(
                List<Ticket> tickets,
                Function<Ticket, LocalDateTime> extractor,
                String granularidad) {
            return tickets.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(
                    fecha -> construirEtiquetaPeriodo(fecha, granularidad),
                    Collectors.collectingAndThen(Collectors.counting(), BigDecimal::valueOf)));
        }

        private Map<String, BigDecimal> agruparConteoPorPeriodoReserva(
                List<Reserva> reservas,
                Function<Reserva, LocalDateTime> extractor,
                String granularidad) {
            return reservas.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(
                    fecha -> construirEtiquetaPeriodo(fecha, granularidad),
                    Collectors.collectingAndThen(Collectors.counting(), BigDecimal::valueOf)));
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

        private RangosComparacion resolverRangosComparacion(
                LocalDateTime fechaDesde,
                LocalDateTime fechaHasta,
                String modoComparacion) {
            RangoFechas actual = resolverRango(fechaDesde, fechaHasta);
            String modo = normalizarModoComparacion(modoComparacion);

            RangoFechas comparado;
            if (MODO_COMPARACION_MISMO_PERIODO_ANIO_ANTERIOR.equals(modo)) {
                comparado = new RangoFechas(
                        actual.fechaDesde().minusYears(1),
                        actual.fechaHasta().minusYears(1));
            } else {
                Duration duracion = Duration.between(actual.fechaDesde(), actual.fechaHasta());
                if (duracion.isZero() || duracion.isNegative()) {
                    duracion = Duration.ofDays(1);
                }
                LocalDateTime comparadoHasta = actual.fechaDesde();
                LocalDateTime comparadoDesde = comparadoHasta.minus(duracion);
                comparado = new RangoFechas(comparadoDesde, comparadoHasta);
            }

            return new RangosComparacion(actual, comparado);
        }

        private String normalizarModoComparacion(String modoComparacion) {
            String value = normalizarTexto(modoComparacion)
                    .replace("_", "")
                    .replace("-", "")
                    .toLowerCase(Locale.ROOT);
            if (value.isBlank()) {
                return MODO_COMPARACION_PERIODO_ANTERIOR;
            }
            if (MODO_COMPARACION_PERIODO_ANTERIOR.equals(value)
                    || MODO_COMPARACION_MISMO_PERIODO_ANIO_ANTERIOR.equals(value)) {
                return value;
            }
            throw new IllegalArgumentException("modoComparacion invalido. Use: periodoAnterior o mismoPeriodoAnioAnterior");
        }

        private List<Reserva> obtenerReservasProgramadasEnRango(RangoFechas rango) {
            return reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                    .filter(reserva -> reserva.getHoraInicio() != null)
                    .filter(reserva -> !reserva.getHoraInicio().isBefore(rango.fechaDesde())
                            && reserva.getHoraInicio().isBefore(rango.fechaHasta()))
                    .toList();
        }

        private boolean existePagoAsociableAReserva(Reserva reserva, List<Pago> pagos) {
            if (reserva == null || reserva.getHoraInicio() == null || reserva.getPlaca() == null || reserva.getEspacio() == null
                    || reserva.getEspacio().getId() == null) {
                return false;
            }

            LocalDateTime ventanaInicio = reserva.getHoraInicio().minusMinutes(30);
            LocalDateTime ventanaFin = reserva.getHoraFin() != null
                    ? reserva.getHoraFin().plusHours(6)
                    : reserva.getHoraInicio().plusHours(24);

            String placaReserva = normalizarTexto(reserva.getPlaca()).toUpperCase(Locale.ROOT);
            Long espacioId = reserva.getEspacio().getId();

            return pagos.stream()
                    .map(Pago::getTicket)
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(ticket -> {
                        if (ticket.getHoraEntrada() == null || ticket.getPlaca() == null || ticket.getEspacio() == null
                                || ticket.getEspacio().getId() == null) {
                            return false;
                        }
                        String placaTicket = normalizarTexto(ticket.getPlaca()).toUpperCase(Locale.ROOT);
                        boolean mismaPlaca = placaReserva.equals(placaTicket);
                        boolean mismoEspacio = espacioId.equals(ticket.getEspacio().getId());
                        boolean enVentana = !ticket.getHoraEntrada().isBefore(ventanaInicio)
                                && !ticket.getHoraEntrada().isAfter(ventanaFin);
                        return mismaPlaca && mismoEspacio && enVentana;
                    });
        }

        private boolean esReservaNoShow(Reserva reserva) {
            if (reserva == null || reserva.getHoraInicio() == null || reserva.getEstado() == null
                    || reserva.getEstado().getNombre() == null) {
                return false;
            }

            String estado = reserva.getEstado().getNombre().trim().toUpperCase(Locale.ROOT);
            if (ESTADO_RESERVA_CANCELADA.equalsIgnoreCase(estado)) {
                String motivo = normalizarTexto(reserva.getMotivoCancelacion()).toLowerCase(Locale.ROOT);
                return motivo.contains("no show") || motivo.contains("noshow");
            }

            return ESTADO_RESERVA_PENDIENTE.equalsIgnoreCase(estado)
                    && reserva.getHoraInicio().isBefore(LocalDateTime.now());
        }

        private BigDecimal calcularPorcentaje(long numerador, long denominador) {
            if (denominador <= 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(numerador)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(denominador), 2, java.math.RoundingMode.HALF_UP);
        }

        private ReporteComparativoItemDTO construirComparativoItem(String etiqueta, BigDecimal actual, BigDecimal comparado) {
            BigDecimal valorActual = actual == null ? BigDecimal.ZERO : actual;
            BigDecimal valorComparado = comparado == null ? BigDecimal.ZERO : comparado;
            BigDecimal variacionAbsoluta = valorActual.subtract(valorComparado);

            BigDecimal variacionPorcentual;
            if (valorComparado.compareTo(BigDecimal.ZERO) == 0) {
                variacionPorcentual = valorActual.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(100);
            } else {
                variacionPorcentual = variacionAbsoluta
                        .multiply(BigDecimal.valueOf(100))
                        .divide(valorComparado, 2, java.math.RoundingMode.HALF_UP);
            }

            return new ReporteComparativoItemDTO(
                    etiqueta,
                    valorActual,
                    valorComparado,
                    variacionAbsoluta,
                    variacionPorcentual);
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

        private String normalizarMotivo(String motivo) {
            if (motivo == null || motivo.trim().isEmpty()) {
                return "SIN_MOTIVO_REGISTRADO";
            }
            return motivo.trim();
        }

        private String normalizarTexto(String value) {
            return value == null ? "" : value.trim();
        }

        private List<Ticket> filtrarPorUsuario(List<Ticket> tickets, Long usuarioId) {
            if (usuarioId == null) {
                return tickets;
            }
            return tickets.stream()
                    .filter(ticket -> ticket.getCreadoPor() != null)
                    .filter(ticket -> ticket.getCreadoPor().getId() != null)
                    .filter(ticket -> usuarioId.equals(ticket.getCreadoPor().getId()))
                    .toList();
        }

        private String obtenerEtiquetaUsuario(Ticket ticket) {
            if (ticket.getCreadoPor() == null) {
                return "-";
            }
            String username = ticket.getCreadoPor().getUsername();
            if (username == null || username.isBlank()) {
                return "-";
            }
            return username;
        }

        private byte[] generarCsv(String[] headers, List<List<String>> filas) {
            try (StringWriter writer = new StringWriter();
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
                for (List<String> fila : filas) {
                    csvPrinter.printRecord(fila);
                }
                csvPrinter.flush();
                return writer.toString().getBytes(StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo generar el archivo CSV", ex);
            }
        }

        private String valorCsv(String value) {
            return value == null || value.isBlank() ? "-" : value.trim();
        }

        private byte[] generarPdfProfesional(
            String titulo,
            String subtitulo,
            List<PdfTablaSeccion> tablas,
            List<PdfGraficaSeccion> graficas) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                Document document = new Document(PageSize.A4, 32f, 32f, 36f, 30f);
                PdfWriter.getInstance(document, outputStream);
                document.open();

                agregarCabeceraPdf(document, titulo, subtitulo);

                for (PdfTablaSeccion tabla : tablas) {
                    agregarSeccionTitulo(document, tabla.titulo());
                    document.add(construirTablaPdf(tabla.headers(), tabla.rows()));
                }

                for (PdfGraficaSeccion grafica : graficas) {
                    agregarSeccionTitulo(document, grafica.titulo());
                    document.add(construirTablaGraficaBarras(grafica.items(), grafica.unidad()));
                }

                document.close();
                return outputStream.toByteArray();
            } catch (DocumentException | IOException ex) {
                throw new IllegalStateException("No se pudo generar el archivo PDF", ex);
            }
        }

        private void agregarCabeceraPdf(Document document, String titulo, String subtitulo) throws DocumentException {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PDF_PRIMARY_COLOR);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_SECONDARY_TEXT);

            Paragraph title = new Paragraph(valorCsv(titulo), titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            document.add(title);

            if (subtitulo != null && !subtitulo.isBlank()) {
                Paragraph subtitle = new Paragraph(subtitulo, subtitleFont);
                subtitle.setAlignment(Element.ALIGN_LEFT);
                subtitle.setSpacingAfter(2f);
                document.add(subtitle);
            }

            Paragraph generated = new Paragraph(
                "Emitido: " + formatDateTime(LocalDateTime.now()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, PDF_SECONDARY_TEXT));
            generated.setSpacingAfter(12f);
            document.add(generated);
        }

        private void agregarSeccionTitulo(Document document, String titulo) throws DocumentException {
            Paragraph sectionTitle = new Paragraph(
                valorCsv(titulo),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PDF_PRIMARY_COLOR));
            sectionTitle.setSpacingBefore(8f);
            sectionTitle.setSpacingAfter(6f);
            document.add(sectionTitle);
        }

        private PdfPTable construirTablaPdf(List<String> headers, List<List<String>> rows) {
            PdfPTable table = new PdfPTable(Math.max(1, headers.size()));
            table.setWidthPercentage(100f);
            table.setSpacingAfter(8f);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_PRIMARY_COLOR);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(valorCsv(header), headerFont));
                cell.setBackgroundColor(PDF_HEADER_BG);
                cell.setPadding(6f);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
            }

            if (rows == null || rows.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Sin datos para mostrar", cellFont));
                empty.setColspan(Math.max(1, headers.size()));
                empty.setPadding(8f);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
                return table;
            }

            for (List<String> row : rows) {
                for (String col : row) {
                    PdfPCell dataCell = new PdfPCell(new Phrase(valorCsv(col), cellFont));
                    dataCell.setPadding(5f);
                    dataCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(dataCell);
                }
            }

            return table;
        }

        private PdfPTable construirTablaGraficaBarras(List<PdfBarItem> items, String unidad) {
            PdfPTable chart = new PdfPTable(new float[] {3.2f, 4.8f, 2f});
            chart.setWidthPercentage(100f);
            chart.setSpacingAfter(10f);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_PRIMARY_COLOR);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            chart.addCell(crearHeaderChartCell("Etiqueta", headerFont));
            chart.addCell(crearHeaderChartCell("Grafica", headerFont));
            chart.addCell(crearHeaderChartCell("Valor", headerFont));

            if (items == null || items.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Sin datos para graficar", textFont));
                empty.setColspan(3);
                empty.setPadding(8f);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                chart.addCell(empty);
                return chart;
            }

            BigDecimal max = items.stream()
                .map(PdfBarItem::valor)
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

            if (max.compareTo(BigDecimal.ZERO) <= 0) {
                max = BigDecimal.ONE;
            }

            for (PdfBarItem item : items) {
                BigDecimal valor = item.valor() == null ? BigDecimal.ZERO : item.valor();
                int porcentaje = valor.multiply(BigDecimal.valueOf(100))
                    .divide(max, 0, RoundingMode.HALF_UP)
                    .intValue();

                chart.addCell(crearDataChartCell(valorCsv(item.etiqueta()), textFont, Element.ALIGN_LEFT));
                chart.addCell(crearBarCell(porcentaje));
                chart.addCell(crearDataChartCell(
                    valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + valorCsv(unidad),
                    textFont,
                    Element.ALIGN_RIGHT));
            }

            return chart;
        }

        private PdfPCell crearHeaderChartCell(String text, Font font) {
            PdfPCell cell = new PdfPCell(new Phrase(text, font));
            cell.setBackgroundColor(PDF_HEADER_BG);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            return cell;
        }

        private PdfPCell crearDataChartCell(String text, Font font, int alignment) {
            PdfPCell cell = new PdfPCell(new Phrase(text, font));
            cell.setPadding(5f);
            cell.setHorizontalAlignment(alignment);
            return cell;
        }

        private PdfPCell crearBarCell(int porcentaje) {
            int clamped = Math.max(0, Math.min(100, porcentaje));
            PdfPTable bar = new PdfPTable(new float[] {clamped <= 0 ? 1 : clamped, clamped >= 100 ? 1 : 100 - clamped});
            bar.setWidthPercentage(100f);

            PdfPCell filled = new PdfPCell(new Phrase(""));
            filled.setBorderWidth(0f);
            filled.setFixedHeight(10f);
            filled.setBackgroundColor(clamped == 0 ? PDF_BAR_BG : PDF_BAR_COLOR);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorderWidth(0f);
            empty.setFixedHeight(10f);
            empty.setBackgroundColor(PDF_BAR_BG);

            bar.addCell(filled);
            bar.addCell(empty);

            PdfPCell wrapper = new PdfPCell(bar);
            wrapper.setPadding(3f);
            return wrapper;
        }

        private List<PdfBarItem> toTopBarItems(Map<String, BigDecimal> source, int limit) {
            return source.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new PdfBarItem(valorCsv(entry.getKey()), entry.getValue() == null ? BigDecimal.ZERO : entry.getValue()))
                .toList();
        }

        private record RangoFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        }

        private record RangosComparacion(RangoFechas actual, RangoFechas comparado) {
        }

        private record PdfTablaSeccion(String titulo, List<String> headers, List<List<String>> rows) {
        }

        private record PdfGraficaSeccion(String titulo, String unidad, List<PdfBarItem> items) {
        }

        private record PdfBarItem(String etiqueta, BigDecimal valor) {
        }
}
