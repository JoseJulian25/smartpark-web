package com.parking.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.AddEspaciosLoteDTO;
import com.parking.dto.EspacioResponseDTO;
import com.parking.dto.ReservaActivaDTO;
import com.parking.dto.TicketActivoDTO;
import com.parking.entity.Espacio;
import com.parking.entity.EstadoEspacio;
import com.parking.entity.Reserva;
import com.parking.entity.Ticket;
import com.parking.entity.TipoVehiculo;
import com.parking.repository.EspacioRepository;
import com.parking.repository.EstadoEspacioRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.TicketRepository;
import com.parking.repository.TipoVehiculoRepository;

@Service
public class EspacioService {

    private static final String ESTADO_LIBRE = "libre";
    private static final String ESTADO_TICKET_ACTIVO = "activo";
    private static final String ESTADO_RESERVA_PENDIENTE = "pendiente";
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final EspacioRepository espacioRepository;
    private final TicketRepository ticketRepository;
    private final ReservaRepository reservaRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final EstadoEspacioRepository estadoEspacioRepository;

    public EspacioService(EspacioRepository espacioRepository, TicketRepository ticketRepository,
            ReservaRepository reservaRepository, TipoVehiculoRepository tipoVehiculoRepository,
            EstadoEspacioRepository estadoEspacioRepository) {
        this.espacioRepository = espacioRepository;
        this.ticketRepository = ticketRepository;
        this.reservaRepository = reservaRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.estadoEspacioRepository = estadoEspacioRepository;
    }

    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarEspacios() {

        List<Espacio> espacios = espacioRepository.findAllByActivoTrueOrderByIdAsc();
        if (espacios.isEmpty()) {
            return List.of();
        }

        List<Long> espacioIds = espacios.stream().map(Espacio::getId).toList();
        Map<Long, Ticket> ticketActivoPorEspacio = obtenerTicketsActivosPorEspacio(espacioIds);
        Map<Long, Reserva> reservaPendientePorEspacio = obtenerReservasPendientesPorEspacio(espacioIds);

        List<EspacioResponseDTO> response = new ArrayList<>();

        for (Espacio espacio : espacios) {
            response.add(toDto(
                    espacio,
                    ticketActivoPorEspacio.get(espacio.getId()),
                    reservaPendientePorEspacio.get(espacio.getId())));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<EspacioResponseDTO> listarEspaciosInactivos() {

        List<Espacio> espacios = espacioRepository.findAllByActivoFalseOrderByIdAsc();
        List<EspacioResponseDTO> response = new ArrayList<>();

        for (Espacio espacio : espacios) {
            response.add(toDto(espacio, null, null));
        }

        return response;
    }

    @Transactional
    public EspacioResponseDTO actualizarEstado(Long id, String nuevoEstado) {

        Espacio espacio = espacioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado"));

        EstadoEspacio estado = estadoEspacioRepository.findByNombreIgnoreCase(nuevoEstado.trim())
                .orElseThrow(() -> new NoSuchElementException("Estado de espacio no existe"));

        espacio.setEstado(estado);
        Espacio actualizado = espacioRepository.save(espacio);

        Optional<Ticket> ticketActivo = ticketRepository
                .findTopByEspacioIdAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(actualizado.getId(), ESTADO_TICKET_ACTIVO);
        Optional<Reserva> reservaActiva = reservaRepository
            .findTopByEspacioIdAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(actualizado.getId(), ESTADO_RESERVA_PENDIENTE);

        return toDto(actualizado, ticketActivo.orElse(null), reservaActiva.orElse(null));
    }

    @Transactional
    public List<EspacioResponseDTO> agregarLote(AddEspaciosLoteDTO dto) {

        int cantidadCarros = dto.getCantidadCarros();
        int cantidadMotos = dto.getCantidadMotos();

        TipoVehiculo tipoCarro = tipoVehiculoRepository.findByNombreIgnoreCase("carro")
                .orElseThrow(() -> new NoSuchElementException("Tipo de vehiculo 'carro' no existe"));
        TipoVehiculo tipoMoto = tipoVehiculoRepository.findByNombreIgnoreCase("moto")
                .orElseThrow(() -> new NoSuchElementException("Tipo de vehiculo 'moto' no existe"));
        EstadoEspacio estadoLibre = estadoEspacioRepository.findByNombreIgnoreCase(ESTADO_LIBRE)
                .orElseThrow(() -> new NoSuchElementException("Estado 'libre' no existe"));

        List<Espacio> nuevos = new ArrayList<>();

        int nextCarro = obtenerSiguienteNumero("C");
        for (int i = 0; i < cantidadCarros; i++) {
            nuevos.add(crearEspacio(tipoCarro, estadoLibre, "C", nextCarro + i));
        }

        int nextMoto = obtenerSiguienteNumero("M");
        for (int i = 0; i < cantidadMotos; i++) {
            nuevos.add(crearEspacio(tipoMoto, estadoLibre, "M", nextMoto + i));
        }

        List<Espacio> guardados = espacioRepository.saveAll(nuevos);
        List<EspacioResponseDTO> response = new ArrayList<>();

        for (Espacio espacio : guardados) {
            response.add(toDto(espacio, null, null));
        }

        return response;
    }

    @Transactional
    public void eliminarEspacio(Long id) {

        Espacio espacio = espacioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado"));

        if (!ESTADO_LIBRE.equalsIgnoreCase(espacio.getEstado().getNombre())) {
            throw new IllegalStateException("Solo se pueden eliminar espacios libres");
        }

        espacio.setActivo(false);
        espacioRepository.save(espacio);
    }

    @Transactional
    public EspacioResponseDTO reactivarEspacio(Long id) {

        Espacio espacio = espacioRepository.findByIdAndActivoFalse(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio inactivo no encontrado"));

        espacio.setActivo(true);
        Espacio reactivado = espacioRepository.save(espacio);

        return toDto(reactivado, null, null);
    }

    private Map<Long, Ticket> obtenerTicketsActivosPorEspacio(List<Long> espacioIds) {
        List<Ticket> ticketsActivos = ticketRepository
                .findAllByEspacioIdInAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(espacioIds, ESTADO_TICKET_ACTIVO);

        Map<Long, Ticket> ticketPorEspacio = new HashMap<>();
        for (Ticket ticket : ticketsActivos) {
            Long espacioId = ticket.getEspacio() == null ? null : ticket.getEspacio().getId();
            if (espacioId != null && !ticketPorEspacio.containsKey(espacioId)) {
                ticketPorEspacio.put(espacioId, ticket);
            }
        }
        return ticketPorEspacio;
    }

    private Map<Long, Reserva> obtenerReservasPendientesPorEspacio(List<Long> espacioIds) {
        List<Reserva> reservasPendientes = reservaRepository
                .findAllByEspacioIdInAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(espacioIds, ESTADO_RESERVA_PENDIENTE);

        Map<Long, Reserva> reservaPorEspacio = new HashMap<>();
        for (Reserva reserva : reservasPendientes) {
            Long espacioId = reserva.getEspacio() == null ? null : reserva.getEspacio().getId();
            if (espacioId != null && !reservaPorEspacio.containsKey(espacioId)) {
                reservaPorEspacio.put(espacioId, reserva);
            }
        }
        return reservaPorEspacio;
    }

    private Espacio crearEspacio(TipoVehiculo tipoVehiculo, EstadoEspacio estadoLibre, String prefijo, int correlativo) {

        Espacio espacio = new Espacio();
        espacio.setCodigoEspacio(generarCodigo(prefijo, correlativo));
        espacio.setTipoVehiculo(tipoVehiculo);
        espacio.setEstado(estadoLibre);
        espacio.setActivo(true);
        return espacio;
    }

    private int obtenerSiguienteNumero(String prefijo) {

        List<Espacio> existentes = espacioRepository.findByCodigoEspacioStartingWith(prefijo + "-");
        Pattern pattern = Pattern.compile("^" + prefijo + "-(\\d+)$");

        int max = 0;
        for (Espacio espacio : existentes) {
            String codigo = normalizarCodigo(espacio.getCodigoEspacio());
            Matcher matcher = pattern.matcher(codigo);
            if (matcher.matches()) {
                int numero = Integer.parseInt(matcher.group(1));
                if (numero > max) {
                    max = numero;
                }
            }
        }

        return max + 1;
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }

        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String generarCodigo(String prefijo, int correlativo) {
        return String.format("%s-%03d", prefijo, correlativo);
    }

    private EspacioResponseDTO toDto(Espacio espacio, Ticket ticketActivo, Reserva reservaActiva) {

        TicketActivoDTO ticketActivoDTO = null;
        if (ticketActivo != null && ticketActivo.getHoraEntrada() != null) {
            ticketActivoDTO = new TicketActivoDTO(
                    ticketActivo.getCodigoTicket(),
                    ticketActivo.getPlaca(),
                    ticketActivo.getHoraEntrada().format(HORA_FORMATTER),
                    ticketActivo.getHoraEntrada()
            );
        }

        ReservaActivaDTO reservaActivaDTO = null;
        if (reservaActiva != null && reservaActiva.getHoraInicio() != null) {
            reservaActivaDTO = new ReservaActivaDTO(
                reserva.getCodigoReserva(),
                reserva.getClienteNombreCompleto(),
                reserva.getPlaca(),
                reserva.getHoraInicio().format(HORA_FORMATTER)
            );
        }

        return new EspacioResponseDTO(
                espacio.getId(),
                espacio.getCodigoEspacio(),
                espacio.getTipoVehiculo().getNombre(),
                espacio.getEstado().getNombre(),
            ticketActivoDTO,
            reservaActivaDTO
        );
    }
}
