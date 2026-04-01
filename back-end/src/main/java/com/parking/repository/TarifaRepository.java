package com.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.entity.Tarifa;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    Optional<Tarifa> findByTipoVehiculoNombreIgnoreCase(String nombreTipoVehiculo);

    List<Tarifa> findAllByOrderByIdAsc();
}