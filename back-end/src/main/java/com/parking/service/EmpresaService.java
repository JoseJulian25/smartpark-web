package com.parking.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.EmpresaDTO;
import com.parking.dto.EmpresaResponseDTO;
import com.parking.entity.Empresa;
import com.parking.repository.EmpresaRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerConfiguracionEmpresa() {
        Empresa empresa = empresaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("No hay empresa configurada"));

        return toDto(empresa);
    }

    @Transactional
    public EmpresaResponseDTO guardarConfiguracionEmpresa(EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findFirstByOrderByIdAsc().orElse(null);

        String nombre = normalize(dto.getNombre());
        String rnc = normalize(dto.getRnc());
        String direccion = normalize(dto.getDireccion());
        String telefono = normalize(dto.getTelefono());
        String email = normalize(dto.getEmail()).toLowerCase();

        if (empresa == null) {
            validarUnicidadCreacion(rnc, email);
            empresa = new Empresa();
        } else {
            validarUnicidadActualizacion(rnc, email, empresa.getId());
        }

        empresa.setNombre(nombre);
        empresa.setRnc(rnc);
        empresa.setDireccion(direccion);
        empresa.setTelefono(telefono);
        empresa.setEmail(email);

        Empresa guardada = empresaRepository.save(empresa);
        return toDto(guardada);
    }

    private void validarUnicidadCreacion(String rnc, String email) {
        if (empresaRepository.existsByRncIgnoreCase(rnc)) {
            throw new IllegalArgumentException("El RNC ya existe");
        }

        if (empresaRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("El email ya existe");
        }
    }

    private void validarUnicidadActualizacion(String rnc, String email, Long empresaId) {
        if (empresaRepository.existsByRncIgnoreCaseAndIdNot(rnc, empresaId)) {
            throw new IllegalArgumentException("El RNC ya existe");
        }

        if (empresaRepository.existsByEmailIgnoreCaseAndIdNot(email, empresaId)) {
            throw new IllegalArgumentException("El email ya existe");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private EmpresaResponseDTO toDto(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getRnc(),
                empresa.getDireccion(),
                empresa.getTelefono(),
                empresa.getEmail()
        );
    }
}