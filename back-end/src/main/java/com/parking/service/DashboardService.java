package com.parking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.DashboardMovimientoHoraDTO;
import com.parking.entity.Ticket;
import com.parking.repository.TicketRepository;

@Service
public class DashboardService {

    private static final String ESTADO_TICKET_ANULADO = "ANULADO";

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<DashboardMovimientoHoraDTO> obtenerMovimientosHoy() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        int[] entradasPorHora = new int[24];
        int[] salidasPorHora = new int[24];

        List<Ticket> entradasHoy = ticketRepository.findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(
                startOfDay,
                endOfDay);
        for (Ticket ticket : entradasHoy) {
            if (ticket.getHoraEntrada() != null && !esTicketAnulado(ticket)) {
                entradasPorHora[ticket.getHoraEntrada().getHour()]++;
            }
        }

        List<Ticket> salidasHoy = ticketRepository.findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(
                startOfDay,
                endOfDay);
        for (Ticket ticket : salidasHoy) {
            if (ticket.getHoraSalida() != null && !esTicketAnulado(ticket)) {
                salidasPorHora[ticket.getHoraSalida().getHour()]++;
            }
        }

        List<DashboardMovimientoHoraDTO> response = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            response.add(new DashboardMovimientoHoraDTO(
                    hour,
                    String.format("%02d:00", hour),
                    entradasPorHora[hour],
                    salidasPorHora[hour]));
        }

        return response;
    }

    private boolean esTicketAnulado(Ticket ticket) {
        return ticket.getEstado() != null
                && ticket.getEstado().getNombre() != null
                && ESTADO_TICKET_ANULADO.equalsIgnoreCase(ticket.getEstado().getNombre());
    }
}
