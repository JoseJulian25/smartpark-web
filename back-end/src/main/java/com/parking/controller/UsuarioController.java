package com.parking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.UsuarioEstadoDTO;
import com.parking.dto.UsuarioResponseDTO;
import com.parking.dto.UsuarioDTO;
import com.parking.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/usuarios")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(activo));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(
            @Validated(UsuarioDTO.OnCreate.class) @RequestBody UsuarioDTO dto
    ) {
        UsuarioResponseDTO creado = usuarioService.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id,
            @Validated(UsuarioDTO.OnUpdate.class) @RequestBody UsuarioDTO dto
    ) {
        UsuarioResponseDTO actualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> actualizarEstado(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id,
            @Valid @RequestBody UsuarioEstadoDTO dto
    ) {
        UsuarioResponseDTO actualizado = usuarioService.actualizarEstado(id, dto.getActivo());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable @Positive(message = "El id debe ser mayor a 0") Long id
    ) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
