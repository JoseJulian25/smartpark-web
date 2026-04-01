package com.parking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.EmpresaDTO;
import com.parking.dto.EmpresaResponseDTO;
import com.parking.service.EmpresaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/configuracion/empresa")
@Validated
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<EmpresaResponseDTO> obtenerEmpresa() {
        return ResponseEntity.ok(empresaService.obtenerConfiguracionEmpresa());
    }

    @PutMapping
    public ResponseEntity<EmpresaResponseDTO> guardarEmpresa(@Valid @RequestBody EmpresaDTO dto) {
        return ResponseEntity.ok(empresaService.guardarConfiguracionEmpresa(dto));
    }
}