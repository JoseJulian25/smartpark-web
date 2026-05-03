package com.parking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.ReservaCancelacionDTO;
import com.parking.dto.ReservaCreateDTO;
import com.parking.dto.ReservaResponseDTO;
import com.parking.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
@Validated
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarReservas() {
        return ResponseEntity.ok(reservaService.listarReservas());
    }

    @GetMapping("/{codigoReserva}")
    public ResponseEntity<ReservaResponseDTO> obtenerReservaPorCodigo(@PathVariable String codigoReserva) {
        return ResponseEntity.ok(reservaService.obtenerReservaPorCodigo(codigoReserva));
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crearReserva(@Valid @RequestBody ReservaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(dto));
    }

    @PatchMapping("/{codigoReserva}/confirmar-llegada")
    public ResponseEntity<ReservaResponseDTO> confirmarLlegada(@PathVariable String codigoReserva) {
        return ResponseEntity.ok(reservaService.confirmarLlegada(codigoReserva));
    }

    @PatchMapping("/{codigoReserva}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable String codigoReserva,
            @Valid @RequestBody ReservaCancelacionDTO dto) {
        return ResponseEntity.ok(reservaService.cancelarReserva(codigoReserva, dto));
    }

    @PatchMapping("/{codigoReserva}/reenviar-correo")
    public ResponseEntity<ReservaResponseDTO> reenviarCorreo(@PathVariable String codigoReserva) {
        return ResponseEntity.ok(reservaService.reenviarCorreoReserva(codigoReserva));
    }
}
