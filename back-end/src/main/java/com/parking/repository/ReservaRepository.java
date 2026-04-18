package com.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.entity.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByOrderByFechaCreacionDesc();

    List<Reserva> findAllByPlacaIgnoreCaseOrderByFechaCreacionDesc(String placa);

    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    Optional<Reserva> findByCodigoReservaIgnoreCase(String codigoReserva);

    Optional<Reserva> findTopByEspacioIdAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(Long espacioId, String estadoNombre);

    List<Reserva> findAllByEspacioIdInAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(List<Long> espacioIds, String estadoNombre);

    Optional<Reserva> findTopByEspacioIdAndPlacaIgnoreCaseAndEstadoNombreIgnoreCaseOrderByHoraInicioDesc(
            Long espacioId, String placa, String estadoNombre);
}
