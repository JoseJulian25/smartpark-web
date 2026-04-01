package com.parking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.TarifaConfigDTO;
import com.parking.dto.TarifaConfigResponseDTO;
import com.parking.service.TarifaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tarifas")
@Validated
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public ResponseEntity<TarifaConfigResponseDTO> obtenerTarifas() {
        return ResponseEntity.ok(tarifaService.obtenerTarifas());
    }

    @PutMapping
    public ResponseEntity<TarifaConfigResponseDTO> actualizarTarifas(@Valid @RequestBody TarifaConfigDTO dto) {
        return ResponseEntity.ok(tarifaService.actualizarTarifas(dto));
    }
}