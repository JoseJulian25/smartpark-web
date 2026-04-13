package com.parking.service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.ReservaCreateDTO;
import com.parking.dto.ReservaCancelacionDTO;
import com.parking.dto.ReservaResponseDTO;
import com.parking.entity.Espacio;
import com.parking.entity.EstadoEspacio;
import com.parking.entity.EstadoReserva;
import com.parking.entity.Reserva;
import com.parking.entity.TipoVehiculo;
import com.parking.entity.Usuario;
import com.parking.exception.EmailDeliveryException;
import com.parking.repository.EspacioRepository;
import com.parking.repository.EstadoEspacioRepository;
import com.parking.repository.EstadoReservaRepository;
import com.parking.repository.ReservaRepository;
import com.parking.repository.TipoVehiculoRepository;
import com.parking.repository.UsuarioRepository;

@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_ESPACIO_LIBRE = "LIBRE";
    private static final String ESTADO_ESPACIO_RESERVADO = "RESERVADO";

    private final ReservaRepository reservaRepository;
    private final EstadoReservaRepository estadoReservaRepository;
    private final EspacioRepository espacioRepository;
    private final EstadoEspacioRepository estadoEspacioRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaEmailService reservaEmailService;

    public ReservaService(ReservaRepository reservaRepository,
            EstadoReservaRepository estadoReservaRepository,
            EspacioRepository espacioRepository,
            EstadoEspacioRepository estadoEspacioRepository,
            TipoVehiculoRepository tipoVehiculoRepository,
            UsuarioRepository usuarioRepository,
            ReservaEmailService reservaEmailService) {
        this.reservaRepository = reservaRepository;
        this.estadoReservaRepository = estadoReservaRepository;
        this.espacioRepository = espacioRepository;
        this.estadoEspacioRepository = estadoEspacioRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaEmailService = reservaEmailService;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarReservas() {
        return reservaRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponseDTO obtenerReservaPorCodigo(String codigoReserva) {
        Reserva reserva = reservaRepository.findByCodigoReserva(normalize(codigoReserva))
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada"));
        return toDto(reserva);
    }

    @Transactional
    public ReservaResponseDTO crearReserva(ReservaCreateDTO dto) {

        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.findByNombreIgnoreCase(normalize(dto.getTipoVehiculo()))
                .orElseThrow(() -> new NoSuchElementException("Tipo de vehiculo no encontrado"));

        Espacio espacio = espacioRepository.findByIdAndActivoTrue(dto.getEspacioId())
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado o inactivo"));

        if (!espacio.getTipoVehiculo().getId().equals(tipoVehiculo.getId())) {
            throw new IllegalArgumentException("El espacio no corresponde al tipo de vehiculo seleccionado");
        }

        String estadoEspacioActual = normalize(espacio.getEstado().getNombre()).toUpperCase(Locale.ROOT);
        if (!ESTADO_ESPACIO_LIBRE.equals(estadoEspacioActual)) {
            throw new IllegalStateException("Solo se puede reservar un espacio en estado LIBRE");
        }

        EstadoReserva estadoReserva = estadoReservaRepository.findByNombreIgnoreCase(ESTADO_PENDIENTE)
            .orElseThrow(() -> new NoSuchElementException("Estado de reserva PENDIENTE no encontrado"));

        EstadoEspacio estadoEspacioReservado = estadoEspacioRepository.findByNombreIgnoreCase(ESTADO_ESPACIO_RESERVADO)
                .orElseThrow(() -> new NoSuchElementException("Estado de espacio RESERVADO no encontrado"));

        Reserva reserva = new Reserva();
        reserva.setCodigoReserva(generarCodigoReserva());
        reserva.setPlaca(normalize(dto.getPlaca()).toUpperCase(Locale.ROOT));
        reserva.setTipoVehiculo(tipoVehiculo);
        reserva.setEspacio(espacio);
        reserva.setHoraInicio(dto.getHoraInicio());
        reserva.setEstado(estadoReserva);
        reserva.setClienteNombreCompleto(normalize(dto.getClienteNombreCompleto()));
        reserva.setClienteTelefono(normalize(dto.getClienteTelefono()));
        reserva.setClienteEmail(normalize(dto.getClienteEmail()).toLowerCase(Locale.ROOT));
        reserva.setCreadoPor(obtenerUsuarioAutenticado());

        Reserva creada = reservaRepository.save(reserva);

        espacio.setEstado(estadoEspacioReservado);
        espacioRepository.save(espacio);

        // Si falla el envio del correo, se lanza excepcion runtime y la transaccion se revierte.
        reservaEmailService.enviarConfirmacionReserva(creada);

        return toDto(creada);
    }

    @Transactional
    public ReservaResponseDTO confirmarLlegada(String codigoReserva) {
        Reserva reserva = reservaRepository.findByCodigoReserva(normalize(codigoReserva))
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada"));

        String estadoActual = normalize(reserva.getEstado().getNombre()).toUpperCase(Locale.ROOT);
        if (!ESTADO_PENDIENTE.equals(estadoActual)) {
            throw new IllegalStateException("Solo se puede confirmar la llegada de una reserva en estado PENDIENTE");
        }

        EstadoReserva estadoActiva = estadoReservaRepository.findByNombreIgnoreCase(ESTADO_ACTIVA)
                .orElseThrow(() -> new NoSuchElementException("Estado de reserva ACTIVA no encontrado"));

        reserva.setEstado(estadoActiva);
        Reserva actualizada = reservaRepository.save(reserva);

        actualizarEstadoEspacio(actualizada, ESTADO_ESPACIO_RESERVADO);

        return toDto(actualizada);
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(String codigoReserva, ReservaCancelacionDTO dto) {
        Reserva reserva = reservaRepository.findByCodigoReserva(normalize(codigoReserva))
                .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada"));

        String estadoActual = normalize(reserva.getEstado().getNombre()).toUpperCase(Locale.ROOT);
        if (!ESTADO_PENDIENTE.equals(estadoActual) && !ESTADO_ACTIVA.equals(estadoActual)) {
            throw new IllegalStateException("Solo se puede cancelar una reserva en estado PENDIENTE o ACTIVA");
        }

        EstadoReserva estadoCancelada = estadoReservaRepository.findByNombreIgnoreCase(ESTADO_CANCELADA)
                .orElseThrow(() -> new NoSuchElementException("Estado de reserva CANCELADA no encontrado"));

        reserva.setEstado(estadoCancelada);
        reserva.setMotivoCancelacion(normalize(dto.getMotivoCancelacion()));
        reserva.setHoraFin(LocalDateTime.now());
        reserva.setCanceladoPor(obtenerUsuarioAutenticado());
        Reserva actualizada = reservaRepository.save(reserva);

        actualizarEstadoEspacio(actualizada, ESTADO_ESPACIO_LIBRE);

        try {
            reservaEmailService.enviarCancelacionReserva(actualizada);
        } catch (EmailDeliveryException ex) {
            log.warn("No se pudo enviar correo de cancelacion para la reserva {}", actualizada.getCodigoReserva(), ex);
        }

        return toDto(actualizada);
    }

    private void actualizarEstadoEspacio(Reserva reserva, String estadoObjetivo) {
        EstadoEspacio estadoEspacio = estadoEspacioRepository.findByNombreIgnoreCase(estadoObjetivo)
                .orElseThrow(() -> new NoSuchElementException("Estado de espacio " + estadoObjetivo + " no encontrado"));
        reserva.getEspacio().setEstado(estadoEspacio);
        espacioRepository.save(reserva.getEspacio());
    }

    private String generarCodigoReserva() {
        return "R-" + System.currentTimeMillis();
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

    private ReservaResponseDTO toDto(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getCodigoReserva(),
                reserva.getPlaca(),
                reserva.getTipoVehiculo().getNombre(),
                reserva.getEspacio().getId(),
                reserva.getEspacio().getCodigoEspacio(),
                reserva.getEstado().getNombre(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getMotivoCancelacion(),
                reserva.getCanceladoPor() == null ? null : reserva.getCanceladoPor().getUsername(),
                reserva.getClienteNombreCompleto(),
                reserva.getClienteTelefono(),
                reserva.getClienteEmail(),
                reserva.getFechaCreacion());
    }
}
