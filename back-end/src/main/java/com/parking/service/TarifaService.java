package com.parking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.TarifaConfigDTO;
import com.parking.dto.TarifaConfigResponseDTO;
import com.parking.entity.Tarifa;
import com.parking.repository.TarifaRepository;
import com.parking.repository.TipoVehiculoRepository;

@Service
public class TarifaService {

    private static final String TIPO_CARRO = "CARRO";
    private static final String TIPO_MOTO = "MOTO";

    private final TarifaRepository tarifaRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;

    public TarifaService(TarifaRepository tarifaRepository, TipoVehiculoRepository tipoVehiculoRepository) {
        this.tarifaRepository = tarifaRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
    }

    @Transactional(readOnly = true)
    public TarifaConfigResponseDTO obtenerTarifas() {
        Tarifa tarifaCarro = obtenerTarifaPorTipo(TIPO_CARRO);
        Tarifa tarifaMoto = obtenerTarifaPorTipo(TIPO_MOTO);

        LocalDateTime actualizadoEn = tarifaCarro.getActualizadoEn();
        if (tarifaMoto.getActualizadoEn() != null
                && (actualizadoEn == null || tarifaMoto.getActualizadoEn().isAfter(actualizadoEn))) {
            actualizadoEn = tarifaMoto.getActualizadoEn();
        }

        return new TarifaConfigResponseDTO(
                tarifaCarro.getPrecioPorFraccion(),
                tarifaMoto.getPrecioPorFraccion(),
                tarifaCarro.getMinutosFraccion(),
                tarifaCarro.getMinutosTolerancia(),
                tarifaCarro.getMinutosMinimo(),
                actualizadoEn
        );
    }

    @Transactional
    public TarifaConfigResponseDTO actualizarTarifas(TarifaConfigDTO dto) {
        Tarifa tarifaCarro = obtenerOCrearTarifaPorTipo(TIPO_CARRO);
        Tarifa tarifaMoto = obtenerOCrearTarifaPorTipo(TIPO_MOTO);

        tarifaCarro.setPrecioPorFraccion(dto.getTarifaCarro());
        tarifaCarro.setMinutosFraccion(dto.getMinutosFraccion());
        tarifaCarro.setMinutosTolerancia(dto.getMinutosTolerancia());
        tarifaCarro.setMinutosMinimo(dto.getMinutosMinimo());

        tarifaMoto.setPrecioPorFraccion(dto.getTarifaMoto());
        tarifaMoto.setMinutosFraccion(dto.getMinutosFraccion());
        tarifaMoto.setMinutosTolerancia(dto.getMinutosTolerancia());
        tarifaMoto.setMinutosMinimo(dto.getMinutosMinimo());

        tarifaRepository.saveAll(List.of(tarifaCarro, tarifaMoto));

        return obtenerTarifas();
    }

    private Tarifa obtenerTarifaPorTipo(String tipoVehiculo) {
        return tarifaRepository.findByTipoVehiculoNombreIgnoreCase(tipoVehiculo)
                .orElseThrow(() -> new NoSuchElementException(
                        "No hay tarifa configurada para tipo de vehiculo: " + tipoVehiculo));
    }

    private Tarifa obtenerOCrearTarifaPorTipo(String tipoVehiculo) {
        return tarifaRepository.findByTipoVehiculoNombreIgnoreCase(tipoVehiculo)
                .orElseGet(() -> {
                    Tarifa nuevaTarifa = new Tarifa();
                    nuevaTarifa.setTipoVehiculo(tipoVehiculoRepository.findByNombreIgnoreCase(tipoVehiculo)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "No existe el tipo de vehiculo: " + tipoVehiculo)));
                    return nuevaTarifa;
                });
    }
}