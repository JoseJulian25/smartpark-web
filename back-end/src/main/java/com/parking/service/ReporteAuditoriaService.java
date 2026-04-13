package com.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.entity.ReporteAuditoria;
import com.parking.repository.ReporteAuditoriaRepository;

@Service
public class ReporteAuditoriaService {

    private static final int MAX_PARAM_LENGTH = 2000;

    private final ReporteAuditoriaRepository reporteAuditoriaRepository;

    public ReporteAuditoriaService(ReporteAuditoriaRepository reporteAuditoriaRepository) {
        this.reporteAuditoriaRepository = reporteAuditoriaRepository;
    }

    @Transactional
    public void registrar(String usuario, String endpoint, String metodo, String parametros, int statusCode) {
        ReporteAuditoria registro = new ReporteAuditoria();
        registro.setUsuario(normalizarUsuario(usuario));
        registro.setEndpoint(truncar(endpoint, 180));
        registro.setMetodo(truncar(metodo, 10));
        registro.setParametros(truncar(parametros, MAX_PARAM_LENGTH));
        registro.setStatusCode(statusCode);
        registro.setExitoso(statusCode >= 200 && statusCode < 400);
        reporteAuditoriaRepository.save(registro);
    }

    private String normalizarUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return "ANONIMO";
        }
        return truncar(usuario, 120);
    }

    private String truncar(String valor, int maxLength) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
