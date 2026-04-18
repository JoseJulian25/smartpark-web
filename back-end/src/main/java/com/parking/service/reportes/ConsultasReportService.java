package com.parking.service.reportes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReporteConsultaPlacaResponseDTO;
import com.parking.dto.ReporteTablaFilaDTO;
import com.parking.dto.ReporteTablaResponseDTO;
import com.parking.entity.Pago;
import com.parking.entity.Reserva;
import com.parking.entity.Ticket;
import com.parking.repository.PagoRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.TicketRepository;
import com.parking.service.reportes.common.ReportesCommonService;
import com.parking.service.reportes.common.ReportesCommonService.RangoFechas;

@Service
public class ConsultasReportService {

        private static final String ESTADO_TICKET_ANULADO = "ANULADO";
    private static final int MAX_RANGE_DIAS = 92;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 200;

    private final TicketRepository ticketRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ReportesCommonService commonService;

    public ConsultasReportService(
            TicketRepository ticketRepository,
            ReservaRepository reservaRepository,
            PagoRepository pagoRepository,
            ReportesCommonService commonService) {
        this.ticketRepository = ticketRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.commonService = commonService;
    }

    @Transactional(readOnly = true)
    public ReporteConsultaPlacaResponseDTO obtenerConsultaPorPlaca(String placa) {
        String placaNormalizada = commonService.normalizarTexto(placa).toUpperCase(Locale.ROOT);
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
                    row.put("horaEntrada", commonService.formatDateTime(ticket.getHoraEntrada()));
                    row.put("horaSalida", commonService.formatDateTime(ticket.getHoraSalida()));
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
                    row.put("horaInicio", commonService.formatDateTime(reserva.getHoraInicio()));
                    row.put("horaFin", commonService.formatDateTime(reserva.getHoraFin()));
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
        String codigoNormalizado = commonService.normalizarTexto(codigoTicket);
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
        row.put("horaEntrada", commonService.formatDateTime(ticket.getHoraEntrada()));
        row.put("horaSalida", commonService.formatDateTime(ticket.getHoraSalida()));
        row.put("montoTotal", ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().toPlainString());

        return new ReporteTablaResponseDTO(
                "Consulta por codigo de ticket",
                columnas,
                List.of(new ReporteTablaFilaDTO(row)),
                1L);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerConsultaPorCodigoReserva(String codigoReserva) {
        String codigoNormalizado = commonService.normalizarTexto(codigoReserva);
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
        row.put("horaInicio", commonService.formatDateTime(reserva.getHoraInicio()));
        row.put("horaFin", commonService.formatDateTime(reserva.getHoraFin()));
        row.put("motivoCancelacion", commonService.normalizarMotivo(reserva.getMotivoCancelacion()));

        return new ReporteTablaResponseDTO(
                "Consulta por codigo de reserva",
                columnas,
                List.of(new ReporteTablaFilaDTO(row)),
                1L);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerListadoTicketsPorFecha(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);

        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getHoraEntrada() == null ? LocalDateTime.MIN : a.getHoraEntrada();
                    LocalDateTime fb = b.getHoraEntrada() == null ? LocalDateTime.MIN : b.getHoraEntrada();
                    return fb.compareTo(fa);
                })
                .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "tipoVehiculo", "codigoEspacio", "estado", "horaEntrada", "horaSalida", "montoTotal");
        List<ReporteTablaFilaDTO> filas = tickets.stream().map(ticket -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("codigoTicket", ticket.getCodigoTicket());
            row.put("placa", ticket.getPlaca());
            row.put("tipoVehiculo", ticket.getTipoVehiculo() == null ? "-" : ticket.getTipoVehiculo().getNombre());
            row.put("codigoEspacio", ticket.getEspacio() == null ? "-" : ticket.getEspacio().getCodigoEspacio());
            row.put("estado", ticket.getEstado() == null ? "-" : ticket.getEstado().getNombre());
            row.put("horaEntrada", commonService.formatDateTime(ticket.getHoraEntrada()));
            row.put("horaSalida", commonService.formatDateTime(ticket.getHoraSalida()));
            row.put("montoTotal", ticket.getMontoTotal() == null ? "-" : ticket.getMontoTotal().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            return new ReporteTablaFilaDTO(row);
        }).toList();

        return paginarTabla("Listado de tickets por fecha", columnas, filas, page, size);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerListadoReservasPorFecha(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);

        List<Reserva> reservas = obtenerReservasCreadasEnRango(rango).stream()
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getFechaCreacion() == null ? LocalDateTime.MIN : a.getFechaCreacion();
                    LocalDateTime fb = b.getFechaCreacion() == null ? LocalDateTime.MIN : b.getFechaCreacion();
                    return fb.compareTo(fa);
                })
                .toList();

        List<String> columnas = List.of("codigoReserva", "placa", "cliente", "tipoVehiculo", "codigoEspacio", "estado", "horaInicio", "horaFin", "fechaCreacion");
        List<ReporteTablaFilaDTO> filas = reservas.stream().map(reserva -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("codigoReserva", reserva.getCodigoReserva());
            row.put("placa", reserva.getPlaca());
            row.put("cliente", reserva.getClienteNombreCompleto());
            row.put("tipoVehiculo", reserva.getTipoVehiculo() == null ? "-" : reserva.getTipoVehiculo().getNombre());
            row.put("codigoEspacio", reserva.getEspacio() == null ? "-" : reserva.getEspacio().getCodigoEspacio());
            row.put("estado", reserva.getEstado() == null ? "-" : reserva.getEstado().getNombre());
            row.put("horaInicio", commonService.formatDateTime(reserva.getHoraInicio()));
            row.put("horaFin", commonService.formatDateTime(reserva.getHoraFin()));
            row.put("fechaCreacion", commonService.formatDateTime(reserva.getFechaCreacion()));
            return new ReporteTablaFilaDTO(row);
        }).toList();

        return paginarTabla("Listado de reservas por fecha", columnas, filas, page, size);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerListadoPagosPorFecha(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);

        List<Pago> pagos = obtenerPagosEnRango(rango).stream()
                .sorted((a, b) -> {
                    LocalDateTime fa = a.getHoraPago() == null ? LocalDateTime.MIN : a.getHoraPago();
                    LocalDateTime fb = b.getHoraPago() == null ? LocalDateTime.MIN : b.getHoraPago();
                    return fb.compareTo(fa);
                })
                .toList();

        List<String> columnas = List.of("codigoTicket", "placa", "tipoVehiculo", "metodoPago", "monto", "horaPago", "procesadoPor");
        List<ReporteTablaFilaDTO> filas = pagos.stream().map(pago -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("codigoTicket", pago.getTicket() == null ? "-" : pago.getTicket().getCodigoTicket());
            row.put("placa", pago.getTicket() == null ? "-" : pago.getTicket().getPlaca());
            row.put("tipoVehiculo", pago.getTicket() == null || pago.getTicket().getTipoVehiculo() == null ? "-" : pago.getTicket().getTipoVehiculo().getNombre());
            row.put("metodoPago", commonService.normalizarMetodoPago(pago.getMetodoPago()));
            row.put("monto", pago.getMonto() == null ? "-" : pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            row.put("horaPago", commonService.formatDateTime(pago.getHoraPago()));
            row.put("procesadoPor", pago.getProcesadoPor() == null || pago.getProcesadoPor().getUsername() == null ? "-" : pago.getProcesadoPor().getUsername());
            return new ReporteTablaFilaDTO(row);
        }).toList();

        return paginarTabla("Listado de pagos por fecha", columnas, filas, page, size);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerConsultaPagoPorCodigoTicket(String codigoTicket) {
        String codigoNormalizado = commonService.normalizarTexto(codigoTicket);
        if (codigoNormalizado.isBlank()) {
            throw new IllegalArgumentException("El codigo de ticket es obligatorio para consultar pago");
        }

        Ticket ticket = ticketRepository.findByCodigoTicketIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new NoSuchElementException("Ticket no encontrado"));

        Pago pago = ticket.getId() == null
                ? null
                : pagoRepository.findFirstByTicket_Id(ticket.getId())
                        .orElseThrow(() -> new NoSuchElementException("No existe pago registrado para este ticket"));

        List<String> columnas = List.of("codigoTicket", "placa", "metodoPago", "monto", "montoRecibido", "horaPago", "procesadoPor");
        Map<String, String> row = new LinkedHashMap<>();
        row.put("codigoTicket", ticket.getCodigoTicket());
        row.put("placa", ticket.getPlaca());
        row.put("metodoPago", pago == null ? "-" : commonService.normalizarMetodoPago(pago.getMetodoPago()));
        row.put("monto", pago == null || pago.getMonto() == null ? "-" : pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        row.put("montoRecibido", pago == null || pago.getMontoRecibido() == null ? "-" : pago.getMontoRecibido().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        row.put("horaPago", pago == null ? "-" : commonService.formatDateTime(pago.getHoraPago()));
        row.put("procesadoPor", pago == null || pago.getProcesadoPor() == null || pago.getProcesadoPor().getUsername() == null ? "-" : pago.getProcesadoPor().getUsername());

        return new ReporteTablaResponseDTO("Consulta de pago por codigo de ticket", columnas, List.of(new ReporteTablaFilaDTO(row)), 1L);
    }

    @Transactional(readOnly = true)
    public ReporteTablaResponseDTO obtenerListadoVehiculosPorFecha(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Integer page,
            Integer size) {
        RangoFechas rango = commonService.resolverRango(fechaDesde, fechaHasta, MAX_RANGE_DIAS);

        List<Ticket> tickets = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                rango.fechaDesde(),
                rango.fechaHasta()).stream()
                .filter(ticket -> !esTicketAnulado(ticket))
                .filter(ticket -> ticket.getPlaca() != null && !ticket.getPlaca().isBlank())
                .toList();

        List<Pago> pagos = obtenerPagosEnRango(rango);
        Map<Long, BigDecimal> montoPorTicket = pagos.stream()
                .filter(pago -> pago.getTicket() != null && pago.getTicket().getId() != null)
                .collect(Collectors.toMap(
                        pago -> pago.getTicket().getId(),
                        pago -> pago.getMonto() == null ? BigDecimal.ZERO : pago.getMonto(),
                        BigDecimal::add));

        Map<String, List<Ticket>> ticketsPorPlaca = tickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getPlaca().trim().toUpperCase(Locale.ROOT),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<String> columnas = List.of("placa", "tipoVehiculo", "entradasEnRango", "ultimoTicket", "ultimoEspacio", "ultimaEntrada", "montoPagadoEnRango");
        List<ReporteTablaFilaDTO> filas = ticketsPorPlaca.entrySet().stream()
                .map(entry -> {
                    List<Ticket> historial = entry.getValue();
                    Ticket ultimo = historial.stream()
                            .max(Comparator.comparing(ticket -> ticket.getHoraEntrada() == null ? LocalDateTime.MIN : ticket.getHoraEntrada()))
                            .orElse(null);

                    BigDecimal monto = historial.stream()
                            .map(Ticket::getId)
                            .filter(java.util.Objects::nonNull)
                            .map(id -> montoPorTicket.getOrDefault(id, BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, java.math.RoundingMode.HALF_UP);

                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("placa", entry.getKey());
                    row.put("tipoVehiculo", ultimo == null || ultimo.getTipoVehiculo() == null ? "-" : ultimo.getTipoVehiculo().getNombre());
                    row.put("entradasEnRango", String.valueOf(historial.size()));
                    row.put("ultimoTicket", ultimo == null ? "-" : ultimo.getCodigoTicket());
                    row.put("ultimoEspacio", ultimo == null || ultimo.getEspacio() == null ? "-" : ultimo.getEspacio().getCodigoEspacio());
                    row.put("ultimaEntrada", ultimo == null ? "-" : commonService.formatDateTime(ultimo.getHoraEntrada()));
                    row.put("montoPagadoEnRango", monto.toPlainString());
                    return new ReporteTablaFilaDTO(row);
                })
                .sorted((a, b) -> {
                    int ca = Integer.parseInt(a.getColumnas().getOrDefault("entradasEnRango", "0"));
                    int cb = Integer.parseInt(b.getColumnas().getOrDefault("entradasEnRango", "0"));
                    return Integer.compare(cb, ca);
                })
                .toList();

        return paginarTabla("Listado de vehiculos por fecha", columnas, filas, page, size);
    }

    private List<Pago> obtenerPagosEnRango(RangoFechas rango) {
        return pagoRepository.findAllByHoraPagoGreaterThanEqualAndHoraPagoLessThan(
                rango.fechaDesde(),
                rango.fechaHasta());
    }

    private List<Reserva> obtenerReservasCreadasEnRango(RangoFechas rango) {
        return reservaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(reserva -> reserva.getFechaCreacion() != null)
                .filter(reserva -> !reserva.getFechaCreacion().isBefore(rango.fechaDesde())
                        && reserva.getFechaCreacion().isBefore(rango.fechaHasta()))
                .toList();
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
                commonService.paginarFilas(filas, page, size, DEFAULT_PAGE, DEFAULT_SIZE, MAX_SIZE),
                (long) filas.size());
    }

        private boolean esTicketAnulado(Ticket ticket) {
                return ticket.getEstado() != null
                                && ticket.getEstado().getNombre() != null
                                && ESTADO_TICKET_ANULADO.equalsIgnoreCase(ticket.getEstado().getNombre());
        }
}
