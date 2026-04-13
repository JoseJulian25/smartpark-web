package com.parking.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.EntradaVehiculoDTO;
import com.parking.dto.EntradaVehiculoResponseDTO;
import com.parking.entity.Espacio;
import com.parking.entity.EstadoEspacio;
import com.parking.entity.EstadoTicket;
import com.parking.entity.Ticket;
import com.parking.entity.TipoVehiculo;
import com.parking.entity.Usuario;
import com.parking.repository.EspacioRepository;
import com.parking.repository.EstadoEspacioRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.EstadoTicketRepository;
import com.parking.repository.TicketRepository;
import com.parking.repository.TipoVehiculoRepository;
import com.parking.repository.UsuarioRepository;

@Service
public class EntradaService {

    private static final String ESTADO_ESPACIO_LIBRE = "LIBRE";
    private static final String ESTADO_ESPACIO_RESERVADO = "RESERVADO";
    private static final String ESTADO_ESPACIO_OCUPADO = "OCUPADO";
    private static final String ESTADO_TICKET_ACTIVO = "ACTIVO";
    private static final String ESTADO_RESERVA_ACTIVA = "ACTIVA";

    private final EspacioRepository espacioRepository;
    private final EstadoEspacioRepository estadoEspacioRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final TicketRepository ticketRepository;
    private final EstadoTicketRepository estadoTicketRepository;
    private final UsuarioRepository usuarioRepository;

    public EntradaService(
            EspacioRepository espacioRepository,
            EstadoEspacioRepository estadoEspacioRepository,
            TipoVehiculoRepository tipoVehiculoRepository,
            ReservaRepository reservaRepository,
            TicketRepository ticketRepository,
            EstadoTicketRepository estadoTicketRepository,
            UsuarioRepository usuarioRepository) {
        this.espacioRepository = espacioRepository;
        this.estadoEspacioRepository = estadoEspacioRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.ticketRepository = ticketRepository;
        this.estadoTicketRepository = estadoTicketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EntradaVehiculoResponseDTO registrarEntrada(EntradaVehiculoDTO dto) {
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.findByNombreIgnoreCase(normalize(dto.getTipoVehiculo()))
                .orElseThrow(() -> new NoSuchElementException("Tipo de vehiculo no encontrado"));

        Espacio espacio = espacioRepository.findByIdAndActivoTrue(dto.getEspacioId())
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado o inactivo"));

        if (!espacio.getTipoVehiculo().getId().equals(tipoVehiculo.getId())) {
            throw new IllegalArgumentException("El espacio no corresponde al tipo de vehiculo indicado");
        }

        String placa = normalize(dto.getPlaca()).toUpperCase(Locale.ROOT);

        String estadoEspacioActual = normalize(espacio.getEstado().getNombre()).toUpperCase(Locale.ROOT);
        if (!ESTADO_ESPACIO_LIBRE.equals(estadoEspacioActual)
            && !ESTADO_ESPACIO_RESERVADO.equals(estadoEspacioActual)) {
            throw new IllegalStateException("Solo se puede registrar entrada en espacios libres o reservados");
        }

        if (ESTADO_ESPACIO_RESERVADO.equals(estadoEspacioActual)) {
            boolean reservaActiva = reservaRepository
                .findTopByEspacioIdAndPlacaIgnoreCaseAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(
                    espacio.getId(),
                    placa,
                    ESTADO_RESERVA_ACTIVA)
                .isPresent();

            if (!reservaActiva) {
            throw new IllegalStateException(
                "El espacio esta reservado. Solo se permite entrada para la reserva activa asociada");
            }
        }

        boolean placaActiva = ticketRepository
                .findTopByPlacaAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(placa, ESTADO_TICKET_ACTIVO)
                .isPresent();

        if (placaActiva) {
            throw new IllegalStateException("La placa ya tiene un ticket activo");
        }

        EstadoTicket estadoTicketActivo = estadoTicketRepository.findByNombreIgnoreCase(ESTADO_TICKET_ACTIVO)
                .orElseThrow(() -> new NoSuchElementException("Estado de ticket ACTIVO no encontrado"));

        EstadoEspacio estadoOcupado = estadoEspacioRepository.findByNombreIgnoreCase(ESTADO_ESPACIO_OCUPADO)
                .orElseThrow(() -> new NoSuchElementException("Estado de espacio OCUPADO no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setCodigoTicket(generarCodigoTicket());
        ticket.setPlaca(placa);
        ticket.setTipoVehiculo(tipoVehiculo);
        ticket.setEspacio(espacio);
        ticket.setHoraEntrada(LocalDateTime.now());
        ticket.setEstado(estadoTicketActivo);
        ticket.setCreadoPor(obtenerUsuarioAutenticado());

        Ticket creado = ticketRepository.save(ticket);

        espacio.setEstado(estadoOcupado);
        espacioRepository.save(espacio);

        return new EntradaVehiculoResponseDTO(
                creado.getCodigoTicket(),
                creado.getPlaca(),
                creado.getTipoVehiculo().getNombre(),
                creado.getEspacio().getId(),
                creado.getEspacio().getCodigoEspacio(),
                creado.getEstado().getNombre(),
                creado.getHoraEntrada());
    }

    private String generarCodigoTicket() {
        return "T-" + System.currentTimeMillis();
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return usuarioRepository.findByUsernameAndActivoTrueAndEliminadoFalse(authentication.getName()).orElse(null);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
