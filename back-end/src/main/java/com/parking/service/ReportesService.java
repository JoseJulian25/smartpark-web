package com.parking.service;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReporteKpiDTO;
import com.parking.dto.ReporteResumenKpiResponseDTO;
import com.parking.dto.ReporteSerieTemporalItemDTO;
import com.parking.dto.ReporteSerieTemporalResponseDTO;
import com.parking.dto.ReporteSeccionDTO;
import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.dto.ReportesBootstrapResponseDTO;
import com.parking.entity.Espacio;
import com.parking.entity.Reserva;
import com.parking.entity.Ticket;
import com.parking.repository.EspacioRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.TicketRepository;

@Service
public class ReportesService {

    private static final String ESTADO_TICKET_ACTIVO = "ACTIVO";
    private static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_RESERVA_ACTIVA = "ACTIVA";
    private static final String ESTADO_RESERVA_FINALIZADA = "FINALIZADA";
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    private static final Set<String> ESTADOS_OCUPADOS_ESPACIO = Set.of("OCUPADO", "RESERVADO");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final TicketRepository ticketRepository;
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;

    public ReportesService(
            TicketRepository ticketRepository,
            ReservaRepository reservaRepository,
            EspacioRepository espacioRepository) {
        this.ticketRepository = ticketRepository;
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
    }

    @Transactional(readOnly = true)
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
        public ReporteSerieTemporalResponseDTO obtenerEntradasPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta());

        return construirSeriePorHora(
            tickets,
            Ticket::getHoraEntrada,
            "Entradas por hora",
            "tickets");
        }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerSalidasPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> tickets = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta());

        return construirSeriePorHora(
            tickets,
            Ticket::getHoraSalida,
            "Salidas por hora",
            "tickets");
        }

        @Transactional(readOnly = true)
        public ReporteSerieTemporalResponseDTO obtenerFlujoNetoPorHora(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Ticket> entradas = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta());
        List<Ticket> salidas = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
            rango.fechaDesde(),
            rango.fechaHasta());

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
        public ReporteTablaResponseDTO obtenerTicketsActivosActuales() {
            List<Ticket> activos = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getEstado() != null)
                    .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                    .sorted((a, b) -> {
                        LocalDateTime first = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                        LocalDateTime second = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                        return second.compareTo(first);
                    })
                    .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "tipoVehiculo", "codigoEspacio", "horaEntrada", "estado");
        List<ReporteTablaFilaDTO> filas = activos.stream()
            .map(ticket -> {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("codigoTicket", ticket.getCodigoTicket());
                row.put("placa", ticket.getPlaca());
                row.put("tipoVehiculo", ticket.getTipoVehiculo().getNombre());
                row.put("codigoEspacio", ticket.getEspacio().getCodigoEspacio());
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
        public ReporteTablaResponseDTO obtenerEstadiasLargas(Integer umbralMinutos) {
        int umbral = umbralMinutos == null || umbralMinutos < 1 ? 360 : umbralMinutos;
        LocalDateTime now = LocalDateTime.now();

            List<Ticket> activos = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getEstado() != null)
                    .filter(ticket -> ESTADO_TICKET_ACTIVO.equalsIgnoreCase(ticket.getEstado().getNombre()))
                    .sorted((a, b) -> {
                        LocalDateTime first = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                        LocalDateTime second = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                        return second.compareTo(first);
                    })
                    .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "codigoEspacio", "horaEntrada", "minutosEstadia");
        List<ReporteTablaFilaDTO> filas = activos.stream()
            .filter(ticket -> ticket.getHoraEntrada() != null)
            .map(ticket -> {
                long minutos = Duration.between(ticket.getHoraEntrada(), now).toMinutes();
                Map<String, String> row = new LinkedHashMap<>();
                row.put("codigoTicket", ticket.getCodigoTicket());
                row.put("placa", ticket.getPlaca());
                row.put("codigoEspacio", ticket.getEspacio().getCodigoEspacio());
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
        RangoFechas rango = resolverRango(fechaDesde, fechaHasta);
        List<Reserva> canceladas = obtenerReservasCanceladasEnRango(rango);

        List<String> columnas = List.of(
                "codigoReserva",
                "cliente",
                "placa",
                "codigoEspacio",
                "horaInicio",
                "horaCancelacion",
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
                    row.put("motivoCancelacion", normalizarMotivo(reserva.getMotivoCancelacion()));
                    return new ReporteTablaFilaDTO(row);
                })
                .toList();

        return new ReporteTablaResponseDTO("Cancelaciones de reservas (detalle)", columnas, filas, (long) filas.size());
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
        if (fechaDesde != null && fechaHasta != null) {
            if (fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalArgumentException("fechaHasta no puede ser menor que fechaDesde");
            }
            return new RangoFechas(fechaDesde, fechaHasta);
        }

        if (fechaDesde != null) {
            return new RangoFechas(fechaDesde, fechaDesde.plusDays(1));
        }

        if (fechaHasta != null) {
            return new RangoFechas(fechaHasta.minusDays(1), fechaHasta);
        }

        LocalDate today = LocalDate.now();
        return new RangoFechas(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
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

        private String normalizarMotivo(String motivo) {
            if (motivo == null || motivo.trim().isEmpty()) {
                return "SIN_MOTIVO_REGISTRADO";
            }
            return motivo.trim();
        }

        private String normalizarTexto(String value) {
            return value == null ? "" : value.trim();
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

        private record RangoFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        }
}
