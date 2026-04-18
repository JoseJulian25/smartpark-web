package com.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.EntradaVehiculoDTO;
import com.parking.dto.EntradaVehiculoResponseDTO;
import com.parking.service.EntradaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/entradas")
@Validated
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @PostMapping
    public ResponseEntity<EntradaVehiculoResponseDTO> registrarEntrada(@Valid @RequestBody EntradaVehiculoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entradaService.registrarEntrada(dto));
    }

    @PatchMapping("/{codigoTicket}/anular")
    public ResponseEntity<EntradaVehiculoResponseDTO> anularTicket(@PathVariable String codigoTicket) {
        return ResponseEntity.ok(entradaService.anularTicket(codigoTicket));
    }
}
