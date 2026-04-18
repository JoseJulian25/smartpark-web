package com.parking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoTicketIgnoreCase(String codigoTicket);

    List<Ticket> findAllByPlacaIgnoreCaseOrderByHoraEntradaDesc(String placa);

    Optional<Ticket> findTopByEspacioIdAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(Long espacioId, String estadoNombre);

    Optional<Ticket> findTopByPlacaAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(String placa, String estadoNombre);

    List<Ticket> findAllByEspacioIdInAndEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(List<Long> espacioIds, String estadoNombre);

    List<Ticket> findAllByHoraEntradaGreaterThanEqualAndHoraEntradaLessThan(LocalDateTime desde, LocalDateTime hasta);

    List<Ticket> findAllByHoraSalidaGreaterThanEqualAndHoraSalidaLessThan(LocalDateTime desde, LocalDateTime hasta);

    List<Ticket> findAllByEstadoNombreIgnoreCaseOrderByHoraEntradaDesc(String estadoNombre);
}
